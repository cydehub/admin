package com.stockzeno.wms.catalog.dto;

import java.time.Instant;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

public class ProductResponse {

    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;
    @Schema(example = "SKU-1000")
    private String sku;
    @Schema(example = "Widget A")
    private String name;
    @Schema(example = "Standard widget")
    private String description;
    @Schema(example = "EA")
    private String unitOfMeasure;
    @Schema(example = "true")
    private boolean active;
    @Schema(example = "2024-01-01T10:00:00Z")
    private Instant createdAt;
    @Schema(example = "2024-01-01T10:00:00Z")
    private Instant updatedAt;

    public ProductResponse(UUID id,
                           String sku,
                           String name,
                           String description,
                           String unitOfMeasure,
                           boolean active,
                           Instant createdAt,
                           Instant updatedAt) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.unitOfMeasure = unitOfMeasure;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
