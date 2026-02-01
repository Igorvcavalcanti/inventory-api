package com.igorcavalcanti.inventory_api.stockmovement.controller;

import com.igorcavalcanti.inventory_api.stockmovement.dto.request.StockMovementRequest;
import com.igorcavalcanti.inventory_api.stockmovement.dto.response.StockMovementResponse;
import com.igorcavalcanti.inventory_api.stockmovement.entity.StockMovementType;
import com.igorcavalcanti.inventory_api.stockmovement.service.StockMovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stock-movement")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StockMovementResponse create(@Valid @RequestBody StockMovementRequest request) {
        return stockMovementService.create(request);
    }

    @GetMapping
    public Page<StockMovementResponse> list(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false)StockMovementType type,
            Pageable pageable
    ) {
        return stockMovementService.list(productId, type, pageable);
    }
}