package com.stockzeno.wms.webhook;

import com.stockzeno.wms.webhook.dto.WebhookEndpointRequest;
import com.stockzeno.wms.webhook.dto.WebhookEndpointResponse;
import com.stockzeno.wms.webhook.dto.WebhookTestRequest;
import com.stockzeno.wms.webhook.dto.WebhookTestResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhooks")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class WebhookController {

    private final WebhookManagementService managementService;

    public WebhookController(WebhookManagementService managementService) {
        this.managementService = managementService;
    }

    @GetMapping
    public ResponseEntity<List<WebhookEndpointResponse>> list() {
        return ResponseEntity.ok(managementService.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WebhookEndpointResponse> get(@PathVariable @NonNull UUID id) {
        return ResponseEntity.ok(managementService.get(id));
    }

    @PostMapping
    public ResponseEntity<WebhookEndpointResponse> create(@Valid @RequestBody WebhookEndpointRequest request) {
        return ResponseEntity.ok(managementService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WebhookEndpointResponse> update(@PathVariable @NonNull UUID id,
                                                          @Valid @RequestBody WebhookEndpointRequest request) {
        return ResponseEntity.ok(managementService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<WebhookEndpointResponse> updateStatus(@PathVariable @NonNull UUID id, @RequestParam boolean active) {
        return ResponseEntity.ok(managementService.updateStatus(id, active));
    }

    @PostMapping("/{id}/test")
    public ResponseEntity<WebhookTestResponse> test(@PathVariable @NonNull UUID id, @RequestBody WebhookTestRequest request) {
        return ResponseEntity.ok(managementService.test(id, request));
    }
}
