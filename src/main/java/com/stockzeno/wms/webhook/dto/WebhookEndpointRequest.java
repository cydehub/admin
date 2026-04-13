package com.stockzeno.wms.webhook.dto;

import com.stockzeno.wms.webhook.WebhookEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class WebhookEndpointRequest {

    @NotBlank
    @Size(max = 300)
    private String url;

    private String secret;

    private List<WebhookEventType> eventTypes;

    private Boolean active;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public List<WebhookEventType> getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(List<WebhookEventType> eventTypes) {
        this.eventTypes = eventTypes;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
