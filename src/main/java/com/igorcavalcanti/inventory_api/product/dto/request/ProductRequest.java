package com.igorcavalcanti.inventory_api.product.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {


    @NotBlank(message = "name is required")
    @Size(max = 150, message = "name must have at most 150 characters")
    private String name;

    @Size(max = 50, message = "sku must have at most 50 character")
    private String sku;

    @Size(max = 500, message = "description must have at most 500 character")
    private String description;

    @DecimalMin(value = "0.0", message = "unitCost must be >= 0")
    private BigDecimal unitCost;

    @DecimalMin(value = "0.0", message = "unitPrice must be >=0")
    private BigDecimal unitPrice;

    @Min(value = 0, message = "initalStock must be >= 0")
    private Integer initialStock;
}
