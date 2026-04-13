package com.stockzeno.wms.inventory.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class AdjustmentResponse {

    private UUID inventoryBalanceId;
    private BigDecimal quantityOnHand;

    public AdjustmentResponse(UUID inventoryBalanceId, BigDecimal quantityOnHand) {
        this.inventoryBalanceId = inventoryBalanceId;
        this.quantityOnHand = quantityOnHand;
    }

    public UUID getInventoryBalanceId() {
        return inventoryBalanceId;
    }

    public BigDecimal getQuantityOnHand() {
        return quantityOnHand;
    }
}
