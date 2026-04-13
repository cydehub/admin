package com.stockzeno.wms.inventory.dto;

import com.stockzeno.wms.inventory.BatchStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class BatchResponse {

    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;
    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID productId;
    @Schema(example = "SKU-1000")
    private String productSku;
    @Schema(example = "LOT-2024-001")
    private String batchCode;
    @Schema(example = "SUP-REF-01")
    private String supplierReference;
    @Schema(example = "2024-01-01")
    private LocalDate manufactureDate;
    @Schema(example = "2025-01-01")
    private LocalDate expiryDate;
    @Schema(example = "ACTIVE")
    private BatchStatus status;
    @Schema(example = "2024-01-01T10:00:00Z")
    private Instant createdAt;
    @Schema(example = "2024-01-01T10:00:00Z")
    private Instant updatedAt;

    public BatchResponse(UUID id,
                         UUID productId,
                         String productSku,
                         String batchCode,
                         String supplierReference,
                         LocalDate manufactureDate,
                         LocalDate expiryDate,
                         BatchStatus status,
                         Instant createdAt,
                         Instant updatedAt) {
        this.id = id;
        this.productId = productId;
        this.productSku = productSku;
        this.batchCode = batchCode;
        this.supplierReference = supplierReference;
        this.manufactureDate = manufactureDate;
        this.expiryDate = expiryDate;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getProductSku() {
        return productSku;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public String getSupplierReference() {
        return supplierReference;
    }

    public LocalDate getManufactureDate() {
        return manufactureDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public BatchStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
