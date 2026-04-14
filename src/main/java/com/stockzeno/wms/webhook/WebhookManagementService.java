package com.stockzeno.wms.webhook;

import com.stockzeno.wms.webhook.dto.WebhookEndpointRequest;
import com.stockzeno.wms.webhook.dto.WebhookEndpointResponse;
import com.stockzeno.wms.webhook.dto.WebhookTestRequest;
import com.stockzeno.wms.webhook.dto.WebhookTestResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WebhookManagementService {

    private final WebhookEndpointRepository endpointRepository;
    private final WebhookService webhookService;

    public WebhookManagementService(WebhookEndpointRepository endpointRepository, WebhookService webhookService) {
        this.endpointRepository = endpointRepository;
        this.webhookService = webhookService;
    }

    @Transactional(readOnly = true)
    public List<WebhookEndpointResponse> list() {
        return endpointRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WebhookEndpointResponse get(@NonNull UUID id) {
        return toResponse(resolveEndpoint(id));
    }

    @Transactional
    public WebhookEndpointResponse create(WebhookEndpointRequest request) {
        WebhookEndpoint endpoint = new WebhookEndpoint();
        endpoint.setUrl(request.getUrl().trim());
        endpoint.setSecret(normalizeSecret(request.getSecret()));
        endpoint.setEventTypes(formatEventTypes(request.getEventTypes()));
        if (request.getActive() != null) {
            endpoint.setActive(request.getActive());
        }
        return toResponse(endpointRepository.save(endpoint));
    }

    @Transactional
    public WebhookEndpointResponse update(@NonNull UUID id, WebhookEndpointRequest request) {
        WebhookEndpoint endpoint = resolveEndpoint(id);
        endpoint.setUrl(request.getUrl().trim());
        if (request.getSecret() != null) {
            endpoint.setSecret(normalizeSecret(request.getSecret()));
        }
        if (request.getEventTypes() != null) {
            endpoint.setEventTypes(formatEventTypes(request.getEventTypes()));
        }
        if (request.getActive() != null) {
            endpoint.setActive(request.getActive());
        }
        return toResponse(endpointRepository.save(endpoint));
    }

    @Transactional
    public WebhookEndpointResponse updateStatus(@NonNull UUID id, boolean active) {
        WebhookEndpoint endpoint = resolveEndpoint(id);
        endpoint.setActive(active);
        return toResponse(endpointRepository.save(endpoint));
    }

    @Transactional
    public WebhookTestResponse test(@NonNull UUID id, WebhookTestRequest request) {
        WebhookEventType eventType = request.getEventType() == null ? WebhookEventType.STOCK_IN : request.getEventType();
        String resourceType = request.getResourceType() == null || request.getResourceType().isBlank()
                ? "test"
                : request.getResourceType();
        WebhookDelivery delivery = webhookService.sendTest(id, eventType, resourceType, request.getResourceId(), request.getData());
        return new WebhookTestResponse(
                delivery.getId(),
                delivery.getStatus(),
                delivery.getAttempts(),
                delivery.getLastResponseCode(),
                delivery.getLastError()
        );
    }

    private WebhookEndpoint resolveEndpoint(@NonNull UUID id) {
        return endpointRepository.findById(Objects.requireNonNull(id, "id"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Webhook endpoint not found"));
    }

    private String formatEventTypes(List<WebhookEventType> eventTypes) {
        if (eventTypes == null || eventTypes.isEmpty()) {
            return null;
        }
        return eventTypes.stream()
                .map(WebhookEventType::name)
                .collect(Collectors.joining(","));
    }

    private List<WebhookEventType> parseEventTypes(String stored) {
        if (stored == null || stored.isBlank()) {
            return List.of();
        }
        Set<String> values = Arrays.stream(stored.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toSet());
        return values.stream()
                .map(WebhookEventType::valueOf)
                .collect(Collectors.toList());
    }

    private WebhookEndpointResponse toResponse(WebhookEndpoint endpoint) {
        boolean secretConfigured = endpoint.getSecret() != null && !endpoint.getSecret().isBlank();
        return new WebhookEndpointResponse(
                endpoint.getId(),
                endpoint.getUrl(),
                parseEventTypes(endpoint.getEventTypes()),
                endpoint.isActive(),
                secretConfigured,
                endpoint.getCreatedAt(),
                endpoint.getUpdatedAt()
        );
    }

    private String normalizeSecret(String secret) {
        if (secret == null) {
            return null;
        }
        String trimmed = secret.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
