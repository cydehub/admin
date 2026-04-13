package com.stockzeno.wms.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

public class BinResponse {

    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;
    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID shelfId;
    @Schema(example = "BIN-01")
    private String code;
    @Schema(example = "Default Bin")
    private String label;
    @Schema(example = "BIN-01")
    private String barcode;
    @Schema(example = "true")
    private boolean active;
    @Schema(example = "2024-01-01T10:00:00Z")
    private Instant createdAt;
    @Schema(example = "2024-01-01T10:00:00Z")
    private Instant updatedAt;

    public BinResponse(UUID id,
                       UUID shelfId,
                       String code,
                       String label,
                       String barcode,
                       boolean active,
                       Instant createdAt,
                       Instant updatedAt) {
        this.id = id;
        this.shelfId = shelfId;
        this.code = code;
        this.label = label;
        this.barcode = barcode;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getShelfId() {
        return shelfId;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getBarcode() {
        return barcode;
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
