package com.stockzeno.wms.inventory.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class TransferResponse {

    private UUID sourceBalanceId;
    private UUID destinationBalanceId;
    private BigDecimal sourceQuantityOnHand;
    private BigDecimal destinationQuantityOnHand;

    public TransferResponse(UUID sourceBalanceId,
                            UUID destinationBalanceId,
                            BigDecimal sourceQuantityOnHand,
                            BigDecimal destinationQuantityOnHand) {
        this.sourceBalanceId = sourceBalanceId;
        this.destinationBalanceId = destinationBalanceId;
        this.sourceQuantityOnHand = sourceQuantityOnHand;
        this.destinationQuantityOnHand = destinationQuantityOnHand;
    }

    public UUID getSourceBalanceId() {
        return sourceBalanceId;
    }

    public UUID getDestinationBalanceId() {
        return destinationBalanceId;
    }

    public BigDecimal getSourceQuantityOnHand() {
        return sourceQuantityOnHand;
    }

    public BigDecimal getDestinationQuantityOnHand() {
        return destinationQuantityOnHand;
    }
}
