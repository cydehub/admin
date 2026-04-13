package com.stockzeno.wms.webhook.dto;

import com.stockzeno.wms.webhook.WebhookDeliveryStatus;
import java.util.UUID;

public class WebhookTestResponse {

    private UUID deliveryId;
    private WebhookDeliveryStatus status;
    private int attempts;
    private Integer lastResponseCode;
    private String lastError;

    public WebhookTestResponse(UUID deliveryId,
                               WebhookDeliveryStatus status,
                               int attempts,
                               Integer lastResponseCode,
                               String lastError) {
        this.deliveryId = deliveryId;
        this.status = status;
        this.attempts = attempts;
        this.lastResponseCode = lastResponseCode;
        this.lastError = lastError;
    }

    public UUID getDeliveryId() {
        return deliveryId;
    }

    public WebhookDeliveryStatus getStatus() {
        return status;
    }

    public int getAttempts() {
        return attempts;
    }

    public Integer getLastResponseCode() {
        return lastResponseCode;
    }

    public String getLastError() {
        return lastError;
    }
}
