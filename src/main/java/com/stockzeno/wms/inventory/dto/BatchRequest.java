package com.stockzeno.wms.inventory.dto;

import com.stockzeno.wms.inventory.BatchStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public class BatchRequest {

    @NotNull
    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID productId;

    @NotBlank
    @Size(max = 120)
    @Schema(example = "LOT-2024-001")
    private String batchCode;

    @Size(max = 160)
    @Schema(example = "SUP-REF-01")
    private String supplierReference;

    @NotNull
    @Schema(example = "2024-01-01")
    private LocalDate manufactureDate;

    @NotNull
    @Schema(example = "2025-01-01")
    private LocalDate expiryDate;

    @Schema(example = "ACTIVE")
    private BatchStatus status;

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public void setBatchCode(String batchCode) {
        this.batchCode = batchCode;
    }

    public String getSupplierReference() {
        return supplierReference;
    }

    public void setSupplierReference(String supplierReference) {
        this.supplierReference = supplierReference;
    }

    public LocalDate getManufactureDate() {
        return manufactureDate;
    }

    public void setManufactureDate(LocalDate manufactureDate) {
        this.manufactureDate = manufactureDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public BatchStatus getStatus() {
        return status;
    }

    public void setStatus(BatchStatus status) {
        this.status = status;
    }
}
