package com.stockzeno.wms.webhook.dto;

import com.stockzeno.wms.webhook.WebhookEventType;
import java.util.Map;
import java.util.UUID;

public class WebhookTestRequest {

    private WebhookEventType eventType;
    private String resourceType;
    private UUID resourceId;
    private Map<String, Object> data;

    public WebhookEventType getEventType() {
        return eventType;
    }

    public void setEventType(WebhookEventType eventType) {
        this.eventType = eventType;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public void setResourceId(UUID resourceId) {
        this.resourceId = resourceId;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
