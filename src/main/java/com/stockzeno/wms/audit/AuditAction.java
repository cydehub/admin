package com.stockzeno.wms.audit;

public enum AuditAction {
    STOCK_IN,
    TRANSFER,
    ADJUSTMENT,
    PICK,
    RETURN,
    LOW_STOCK,
    BATCH_NEAR_EXPIRY,
    BATCH_EXPIRED,
    RECONCILIATION
}
