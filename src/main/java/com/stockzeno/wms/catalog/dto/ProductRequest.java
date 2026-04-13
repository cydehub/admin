package com.stockzeno.wms.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public class ProductRequest {

    @NotBlank
    @Size(max = 80)
    @Schema(example = "SKU-1000")
    private String sku;

    @NotBlank
    @Size(max = 160)
    @Schema(example = "Widget A")
    private String name;

    @Size(max = 500)
    @Schema(example = "Standard widget")
    private String description;

    @Size(max = 40)
    @Schema(example = "EA")
    private String unitOfMeasure;

    @Schema(example = "true")
    private Boolean active;

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
