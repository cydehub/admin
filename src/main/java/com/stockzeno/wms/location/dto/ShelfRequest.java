package com.stockzeno.wms.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public class ShelfRequest {

    @NotNull
    @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID aisleId;

    @NotBlank
    @Size(max = 60)
    @Schema(example = "S-01")
    private String code;

    @NotBlank
    @Size(max = 160)
    @Schema(example = "Shelf 1")
    private String name;

    @Schema(example = "true")
    private Boolean active;

    public UUID getAisleId() {
        return aisleId;
    }

    public void setAisleId(UUID aisleId) {
        this.aisleId = aisleId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
