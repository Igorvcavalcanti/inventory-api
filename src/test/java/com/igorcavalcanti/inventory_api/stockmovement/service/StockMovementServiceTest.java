package com.igorcavalcanti.inventory_api.stockmovement.service;

import com.igorcavalcanti.inventory_api.product.entity.Product;
import com.igorcavalcanti.inventory_api.product.repository.ProductRepository;
import com.igorcavalcanti.inventory_api.stockmovement.dto.request.StockMovementRequest;
import com.igorcavalcanti.inventory_api.stockmovement.entity.StockMovement;
import com.igorcavalcanti.inventory_api.stockmovement.entity.StockMovementType;
import com.igorcavalcanti.inventory_api.stockmovement.repository.StockMovementRepository;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockMovementServiceTest {

    @Mock
    StockMovementRepository stockMovementRepository;
    @Mock
    ProductRepository productRepository;

    @InjectMocks
    StockMovementService service;

    @Test
    void create_whenDuplicateIdempotencyKey_shouldReturnExistingMovement() {
        // ARRANGE
        var req = StockMovementRequest.builder()
                .productId(10L)
                .type(StockMovementType.IN)
                .quantity(5)
                .reason("Reposicao")
                .idempotencyKey("same-key")
                .build();

        var product = new Product();
        product.setId(10L);

        var existing = new StockMovement();
        existing.setId(99L);
        existing.setIdempotencyKey("same-key");
        existing.setProduct(product); // <<< ISSO remove o NPE

        when(stockMovementRepository.findByIdempotencyKey("same-key"))
                .thenReturn(Optional.of(existing));

        // ACT
        var resp = service.create(req);

        // ASSERT (idempotência: não salva de novo)
        assertThat(resp.getId()).isEqualTo(99L);
        assertThat(resp.getProductId()).isEqualTo(10L);

        verify(stockMovementRepository).findByIdempotencyKey("same-key");
        verify(stockMovementRepository, never()).save(any());
        verify(productRepository, never()).findById(any());
    }




    @Test
    void create_whenSameIdempotencyKeyTwice_shouldSaveOnceAndReturnSame() {
        var req = StockMovementRequest.builder()
                .productId(10L)
                .type(StockMovementType.IN)
                .quantity(5)
                .reason("Reposicao")
                .idempotencyKey("same-key")
                .build();


        var product = new Product(); product.setId(10L); product.setCurrentStock(0);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        var saved = new StockMovement(); saved.setId(99L); saved.setIdempotencyKey("same-key"); saved.setProduct(product);

        // 1ª vez: não existe -> salva
        // 2ª vez: existe -> retorna
        when(stockMovementRepository.findByIdempotencyKey("same-key"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(saved));

        when(stockMovementRepository.save(any(StockMovement.class))).thenReturn(saved);

        var r1 = service.create(req);
        var r2 = service.create(req);

        verify(stockMovementRepository, times(1)).save(any(StockMovement.class));
        verify(stockMovementRepository, times(2)).findByIdempotencyKey("same-key");
    }

    @Test
    void create_whenDuplicateKeyByConstraint_shouldReturnExisting() {
        var req = StockMovementRequest.builder()
                .productId(10L)
                .type(StockMovementType.IN)
                .quantity(5)
                .reason("Reposicao")
                .idempotencyKey("same-key")
                .build();

        var product = new Product(); product.setId(10L); product.setCurrentStock(0);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        var existing = new StockMovement(); existing.setId(99L); existing.setIdempotencyKey("same-key"); existing.setProduct(product);

        when(stockMovementRepository.findByIdempotencyKey("same-key"))
                .thenReturn(Optional.empty())           // pré-check não encontra
                .thenReturn(Optional.of(existing));      // depois do save falhar, encontra

        when(stockMovementRepository.save(any(StockMovement.class)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("dup"));

        var resp = service.create(req);

        verify(stockMovementRepository).save(any(StockMovement.class));
        verify(stockMovementRepository, times(2)).findByIdempotencyKey("same-key");
    }


}
