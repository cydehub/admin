package com.stockzeno.wms.analytics.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class ReorderSuggestionResponse {

    private UUID productId;
    private String sku;
    private BigDecimal availableQuantity;
    private BigDecimal reorderPoint;
    private BigDecimal reorderQuantity;
    private boolean shouldReorder;

    public ReorderSuggestionResponse(UUID productId,
                                     String sku,
                                     BigDecimal availableQuantity,
                                     BigDecimal reorderPoint,
                                     BigDecimal reorderQuantity,
                                     boolean shouldReorder) {
        this.productId = productId;
        this.sku = sku;
        this.availableQuantity = availableQuantity;
        this.reorderPoint = reorderPoint;
        this.reorderQuantity = reorderQuantity;
        this.shouldReorder = shouldReorder;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getSku() {
        return sku;
    }

    public BigDecimal getAvailableQuantity() {
        return availableQuantity;
    }

    public BigDecimal getReorderPoint() {
        return reorderPoint;
    }

    public BigDecimal getReorderQuantity() {
        return reorderQuantity;
    }

    public boolean isShouldReorder() {
        return shouldReorder;
    }
}
