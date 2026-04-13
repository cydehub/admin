package com.stockzeno.wms.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WebhookService {

    private final WebhookEndpointRepository endpointRepository;
    private final WebhookDeliveryRepository deliveryRepository;
    private final WebhookProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public WebhookService(WebhookEndpointRepository endpointRepository,
                          WebhookDeliveryRepository deliveryRepository,
                          WebhookProperties properties,
                          RestClient.Builder restClientBuilder,
                          ObjectMapper objectMapper) {
        this.endpointRepository = endpointRepository;
        this.deliveryRepository = deliveryRepository;
        this.properties = properties;
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public void dispatchEvent(WebhookEventType eventType, String resourceType, UUID resourceId, Map<String, Object> data) {
        WebhookPayload payload = new WebhookPayload(eventType, resourceType, resourceId, Instant.now(), data);
        String payloadJson = serializePayload(payload);

        for (WebhookEndpoint endpoint : endpointRepository.findByActiveTrue()) {
            if (!supportsEvent(endpoint, eventType)) {
                continue;
            }
            WebhookDelivery delivery = new WebhookDelivery(endpoint, eventType, resourceType, resourceId, payloadJson);
            attemptDelivery(endpoint, payload, payloadJson, delivery);
            deliveryRepository.save(delivery);
        }
    }

    public WebhookDelivery sendTest(UUID endpointId,
                                    WebhookEventType eventType,
                                    String resourceType,
                                    UUID resourceId,
                                    Map<String, Object> data) {
        WebhookEndpoint endpoint = endpointRepository.findById(endpointId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Webhook endpoint not found"));
        Map<String, Object> payloadData = data == null ? Collections.emptyMap() : data;
        WebhookPayload payload = new WebhookPayload(eventType, resourceType, resourceId, Instant.now(), payloadData);
        String payloadJson = serializePayload(payload);
        WebhookDelivery delivery = new WebhookDelivery(endpoint, eventType, resourceType, resourceId, payloadJson);
        attemptDelivery(endpoint, payload, payloadJson, delivery);
        return deliveryRepository.save(delivery);
    }

    private void attemptDelivery(WebhookEndpoint endpoint,
                                 WebhookPayload payload,
                                 String payloadJson,
                                 WebhookDelivery delivery) {
        int attempts = 0;
        boolean success = false;
        String lastError = null;
        Integer lastCode = null;

        while (attempts < properties.getMaxAttempts() && !success) {
            attempts++;
            try {
                String signature = computeSignature(endpoint.getSecret(), payloadJson);
                var request = restClient.post()
                        .uri(endpoint.getUrl())
                        .header("X-Webhook-Event", payload.getEventType().name());

                if (signature != null) {
                    request = request.header("X-Webhook-Signature", signature);
                }

                var response = payloadJson == null
                        ? request.body(payload).retrieve().toBodilessEntity()
                        : request.contentType(MediaType.APPLICATION_JSON).body(payloadJson).retrieve().toBodilessEntity();
                HttpStatusCode statusCode = response.getStatusCode();
                lastCode = statusCode.value();
                if (statusCode.is2xxSuccessful()) {
                    success = true;
                } else {
                    lastError = "HTTP " + statusCode.value();
                }
            } catch (Exception ex) {
                lastError = ex.getMessage();
            }
        }

        delivery.setAttempts(attempts);
        delivery.setLastResponseCode(lastCode);
        delivery.setLastError(lastError);
        delivery.setLastAttemptAt(Instant.now());
        delivery.setStatus(success ? WebhookDeliveryStatus.SUCCESS : WebhookDeliveryStatus.FAILED);
    }

    private boolean supportsEvent(WebhookEndpoint endpoint, WebhookEventType eventType) {
        String eventTypes = endpoint.getEventTypes();
        if (eventTypes == null || eventTypes.isBlank()) {
            return true;
        }
        Set<String> allowed = Arrays.stream(eventTypes.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toSet());
        return allowed.contains(eventType.name());
    }

    private String serializePayload(WebhookPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    private String computeSignature(String secret, String payloadJson) {
        if (secret == null || secret.isBlank() || payloadJson == null) {
            return null;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(payloadJson.getBytes(StandardCharsets.UTF_8));
            return "sha256=" + HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            return null;
        }
    }
}
