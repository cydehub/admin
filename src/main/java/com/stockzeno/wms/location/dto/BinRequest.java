package com.stockzeno.wms.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public class BinRequest {

    @NotNull
    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID shelfId;

    @NotBlank
    @Size(max = 60)
    @Schema(example = "BIN-01")
    private String code;

    @Size(max = 160)
    @Schema(example = "Default Bin")
    private String label;

    @Size(max = 160)
    @Schema(example = "BIN-01")
    private String barcode;

    @Schema(example = "true")
    private Boolean active;

    public UUID getShelfId() {
        return shelfId;
    }

    public void setShelfId(UUID shelfId) {
        this.shelfId = shelfId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
