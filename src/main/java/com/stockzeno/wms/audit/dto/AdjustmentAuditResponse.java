package com.stockzeno.wms.audit.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class AdjustmentAuditResponse {

    private UUID id;
    private UUID inventoryBalanceId;
    private BigDecimal quantityDelta;
    private String reasonCode;
    private Instant createdAt;
    private UUID userId;

    public AdjustmentAuditResponse(UUID id,
                                   UUID inventoryBalanceId,
                                   BigDecimal quantityDelta,
                                   String reasonCode,
                                   Instant createdAt,
                                   UUID userId) {
        this.id = id;
        this.inventoryBalanceId = inventoryBalanceId;
        this.quantityDelta = quantityDelta;
        this.reasonCode = reasonCode;
        this.createdAt = createdAt;
        this.userId = userId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getInventoryBalanceId() {
        return inventoryBalanceId;
    }

    public BigDecimal getQuantityDelta() {
        return quantityDelta;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public UUID getUserId() {
        return userId;
    }
}
