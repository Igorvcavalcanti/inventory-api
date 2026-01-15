package com.igorcavalcanti.inventory_api.stockmovement.dto.request;

import com.igorcavalcanti.inventory_api.stockmovement.entity.StockMovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StockMovementRequest {

    @NotNull(message = "productId is required")
    private Long productId;

    @NotNull(message = "type is required")
    private StockMovementType type;

    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "quantity must be >=1")
    private Integer quantity;

    @Size(max = 255, message = "reason must have at most 255 characters")
    private String reason;

    @NotBlank(message = "idempotencyKey is required")
    @Size(max = 64, message = "idempotencyKey must have at most 64 cgaratcers")
    private String idempotencyKey;
}