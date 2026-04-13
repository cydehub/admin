package com.stockzeno.wms.analytics.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class StockVelocityResponse {

    private UUID productId;
    private Instant from;
    private Instant to;
    private BigDecimal inboundQuantity;
    private BigDecimal outboundQuantity;
    private BigDecimal netMovement;
    private BigDecimal averageDailyOutbound;

    public StockVelocityResponse(UUID productId,
                                 Instant from,
                                 Instant to,
                                 BigDecimal inboundQuantity,
                                 BigDecimal outboundQuantity,
                                 BigDecimal netMovement,
                                 BigDecimal averageDailyOutbound) {
        this.productId = productId;
        this.from = from;
        this.to = to;
        this.inboundQuantity = inboundQuantity;
        this.outboundQuantity = outboundQuantity;
        this.netMovement = netMovement;
        this.averageDailyOutbound = averageDailyOutbound;
    }

    public UUID getProductId() {
        return productId;
    }

    public Instant getFrom() {
        return from;
    }

    public Instant getTo() {
        return to;
    }

    public BigDecimal getInboundQuantity() {
        return inboundQuantity;
    }

    public BigDecimal getOutboundQuantity() {
        return outboundQuantity;
    }

    public BigDecimal getNetMovement() {
        return netMovement;
    }

    public BigDecimal getAverageDailyOutbound() {
        return averageDailyOutbound;
    }
}
