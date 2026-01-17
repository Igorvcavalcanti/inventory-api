package com.igorcavalcanti.inventory_api.stockmovement.dto.response;


import com.igorcavalcanti.inventory_api.stockmovement.entity.StockMovementType;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class StockMovementResponse {
    private Long id;
    private Long productId;
    private StockMovementType type;
    private Integer quantity;
    private String reason;
    private OffsetDateTime createdAt;
    private Integer currentStock;
}
