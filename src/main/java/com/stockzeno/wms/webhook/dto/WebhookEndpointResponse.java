package com.stockzeno.wms.webhook.dto;

import com.stockzeno.wms.webhook.WebhookEventType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class WebhookEndpointResponse {

    private UUID id;
    private String url;
    private List<WebhookEventType> eventTypes;
    private boolean active;
    private boolean secretConfigured;
    private Instant createdAt;
    private Instant updatedAt;

    public WebhookEndpointResponse(UUID id,
                                   String url,
                                   List<WebhookEventType> eventTypes,
                                   boolean active,
                                   boolean secretConfigured,
                                   Instant createdAt,
                                   Instant updatedAt) {
        this.id = id;
        this.url = url;
        this.eventTypes = eventTypes;
        this.active = active;
        this.secretConfigured = secretConfigured;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public List<WebhookEventType> getEventTypes() {
        return eventTypes;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isSecretConfigured() {
        return secretConfigured;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
