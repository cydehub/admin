package com.stockzeno.wms.webhook;

public enum WebhookEventType {
    STOCK_IN,
    TRANSFER,
    ADJUSTMENT,
    LOW_STOCK,
    BATCH_NEAR_EXPIRY,
    BATCH_EXPIRED
}
