package com.stockzeno.wms.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class WarehouseRequest {

    @NotBlank
    @Size(max = 60)
    @Schema(example = "WH-001")
    private String code;

    @NotBlank
    @Size(max = 160)
    @Schema(example = "Main Warehouse")
    private String name;

    @Schema(example = "true")
    private Boolean active;

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
