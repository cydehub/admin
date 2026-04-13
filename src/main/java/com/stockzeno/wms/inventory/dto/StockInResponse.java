package com.stockzeno.wms.inventory.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class StockInResponse {

    private UUID batchId;
    private UUID inventoryBalanceId;
    private BigDecimal quantityOnHand;

    public StockInResponse(UUID batchId, UUID inventoryBalanceId, BigDecimal quantityOnHand) {
        this.batchId = batchId;
        this.inventoryBalanceId = inventoryBalanceId;
        this.quantityOnHand = quantityOnHand;
    }

    public UUID getBatchId() {
        return batchId;
    }

    public UUID getInventoryBalanceId() {
        return inventoryBalanceId;
    }

    public BigDecimal getQuantityOnHand() {
        return quantityOnHand;
    }
}
