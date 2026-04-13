package com.stockzeno.wms.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

public class WarehouseResponse {

    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;
    @Schema(example = "WH-001")
    private String code;
    @Schema(example = "Main Warehouse")
    private String name;
    @Schema(example = "true")
    private boolean active;
    @Schema(example = "2024-01-01T10:00:00Z")
    private Instant createdAt;
    @Schema(example = "2024-01-01T10:00:00Z")
    private Instant updatedAt;

    public WarehouseResponse(UUID id,
                             String code,
                             String name,
                             boolean active,
                             Instant createdAt,
                             Instant updatedAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
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
