package com.stockzeno.wms.webhook;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class WebhookPayload {

    private WebhookEventType eventType;
    private String resourceType;
    private UUID resourceId;
    private Instant occurredAt;
    private Map<String, Object> data;

    public WebhookPayload(WebhookEventType eventType, String resourceType, UUID resourceId, Instant occurredAt, Map<String, Object> data) {
        this.eventType = eventType;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.occurredAt = occurredAt;
        this.data = data;
    }

    public WebhookEventType getEventType() {
        return eventType;
    }

    public String getResourceType() {
        return resourceType;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
