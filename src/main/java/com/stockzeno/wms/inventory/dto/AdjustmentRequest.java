package com.stockzeno.wms.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class AdjustmentRequest {

    @NotNull
    private UUID productId;

    @NotNull
    private UUID batchId;

    @NotNull
    private UUID binId;

    @NotNull
    private BigDecimal quantityDelta;

    @NotBlank
    private String reasonCode;

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getBatchId() {
        return batchId;
    }

    public void setBatchId(UUID batchId) {
        this.batchId = batchId;
    }

    public UUID getBinId() {
        return binId;
    }

    public void setBinId(UUID binId) {
        this.binId = binId;
    }

    public BigDecimal getQuantityDelta() {
        return quantityDelta;
    }

    public void setQuantityDelta(BigDecimal quantityDelta) {
        this.quantityDelta = quantityDelta;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }
}
