package com.stockzeno.wms.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public class TransferRequest {

    @NotNull
    private UUID productId;

    @NotNull
    private UUID batchId;

    @NotNull
    private UUID fromBinId;

    @NotNull
    private UUID toBinId;

    @NotNull
    @Positive
    private BigDecimal quantity;

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

    public UUID getFromBinId() {
        return fromBinId;
    }

    public void setFromBinId(UUID fromBinId) {
        this.fromBinId = fromBinId;
    }

    public UUID getToBinId() {
        return toBinId;
    }

    public void setToBinId(UUID toBinId) {
        this.toBinId = toBinId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }
}
