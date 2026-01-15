package com.igorcavalcanti.inventory_api.stockmovement.service;


import com.igorcavalcanti.inventory_api.product.entity.Product;
import com.igorcavalcanti.inventory_api.product.repository.ProductRepository;
import com.igorcavalcanti.inventory_api.product.service.ProductNotFoundException;
import com.igorcavalcanti.inventory_api.stockmovement.dto.request.StockMovementRequest;
import com.igorcavalcanti.inventory_api.stockmovement.dto.response.StockMovementResponse;
import com.igorcavalcanti.inventory_api.stockmovement.entity.StockMovement;
import com.igorcavalcanti.inventory_api.stockmovement.entity.StockMovementType;
import com.igorcavalcanti.inventory_api.stockmovement.repository.StockMovementRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;

    @Transactional
    public StockMovementResponse create(StockMovementRequest request) {

        var existing = stockMovementRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existing.isPresent()){
            return toResponse(existing.get());
        }
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        int qty = request.getQuantity();

        if (request.getType() == StockMovementType.OUT) {
            int current = product.getCurrentStock();
            if (current < qty) {
                throw new InsufficientStockException(product.getId(), current, qty);
            }
            product.setCurrentStock(current = qty);
        } else {
            product.setCurrentStock(product.getCurrentStock() + qty);
        }

        StockMovement movement = StockMovement.builder()
                .product(product)
                .type(request.getType())
                .quantity(qty)
                .reason(request.getReason())
                .idempotencyKey(request.getIdempotencyKey())
                .build();

        try {
            StockMovement saved = stockMovementRepository.save(movement);
            return toResponse(saved);

        } catch (DataIntegrityViolationException e){
            // concorrencia: outro request inseriu o mesmo idempotencyKey
            StockMovement saved = stockMovementRepository.findByIdempotencyKey(request.getIdempotencyKey())
                    .orElseThrow(() -> e); // se nao achar, re-lanca (algo realmente estranho)

            return toResponse(saved);
        }



    }

    @Transactional(readOnly = true)
    public Page<StockMovementResponse> list(Long productId, StockMovementType type, Pageable pageable) {
        Page<StockMovement> page;

        if (productId != null && type != null) {
            page = stockMovementRepository.findByProductIdAndType(productId, type, pageable);
        } else if (productId != null) {
            page = stockMovementRepository.findByProductId(productId, pageable);
        } else if (type != null) {
            page = stockMovementRepository.findByType(type, pageable);
        } else {
            page = stockMovementRepository.findAll(pageable);
        }

        return  page.map(this::toResponse);
    }

    private StockMovementResponse toResponse(StockMovement movement) {
        return StockMovementResponse.builder()
                .id(movement.getId())
                .productId(movement.getProduct().getId())
                .type(movement.getType())
                .quantity(movement.getQuantity())
                .reason(movement.getReason())
                .createdAt(movement.getCreatedAt())
                .build();
    }
}
