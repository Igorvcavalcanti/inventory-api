package com.igorcavalcanti.inventory_api.stockmovement.repository;

import com.igorcavalcanti.inventory_api.stockmovement.entity.StockMovement;
import com.igorcavalcanti.inventory_api.stockmovement.entity.StockMovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    Page<StockMovement> findByProductId(Long product, Pageable pageable);

    Page<StockMovement> findByType(StockMovementType type, Pageable pageable);

    Page<StockMovement> findByProductIdAndType(Long product, StockMovementType type, Pageable pageable);

    Optional<StockMovement> findByIdempotencyKey(String idempotencyKey);
}
