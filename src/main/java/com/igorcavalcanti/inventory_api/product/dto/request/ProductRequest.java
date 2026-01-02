package com.igorcavalcanti.inventory_api.product.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    private String name;
    private String sku;
    private String description;
    private BigDecimal unitCost;
    private BigDecimal unitPrice;
    private Integer initialStock;
}
