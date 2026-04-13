package com.stockzeno.wms.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class FefoSuggestion {

    private UUID batchId;
    private String batchCode;
    private LocalDate expiryDate;
    private BigDecimal availableQuantity;

    public FefoSuggestion(UUID batchId, String batchCode, LocalDate expiryDate, BigDecimal availableQuantity) {
        this.batchId = batchId;
        this.batchCode = batchCode;
        this.expiryDate = expiryDate;
        this.availableQuantity = availableQuantity;
    }

    public UUID getBatchId() {
        return batchId;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public BigDecimal getAvailableQuantity() {
        return availableQuantity;
    }
}
