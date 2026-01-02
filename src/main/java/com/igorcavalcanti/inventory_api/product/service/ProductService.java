package com.igorcavalcanti.inventory_api.product.service;


import com.igorcavalcanti.inventory_api.product.dto.request.ProductRequest;
import com.igorcavalcanti.inventory_api.product.dto.response.ProductResponse;
import com.igorcavalcanti.inventory_api.product.entity.Product;
import com.igorcavalcanti.inventory_api.product.repository.ProductRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse create(ProductRequest request){
        Product product = Product.builder()
                .name(request.getName())
                .sku(request.getSku())
                .description(request.getDescription())
                .unitCost(request.getUnitCost())
                .unitPrice(request.getUnitPrice())
                .currentStock(request.getInitialStock() != null ? request.getInitialStock() : 0)
                .active(true)
                .build();

        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> list(String name, Boolean onlyActive, Pageable pageable) {
        Page<Product> page;

        boolean hasName = name != null && !name.isBlank();

        if (Boolean.TRUE.equals(onlyActive) && hasName) {
            page = productRepository.findByActiveTrueAndNameContainingIgnoreCase(name, pageable);
        } else if (Boolean.TRUE.equals(onlyActive)) {
            page = productRepository.findByActiveTrue(pageable);
        } else if (hasName) {
            page = productRepository.findByNameContainingIgnoreCase(name, pageable);
        } else {
            page = productRepository.findAll(pageable);
        }

        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return toResponse(product);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setDescription(request.getDescription());
        product.setUnitCost(request.getUnitCost());
        product.setUnitPrice(request.getUnitPrice());
        // estoque nao e alterado aqui

        Product updated = productRepository.save(product);
        return toResponse(updated);
    }

    @Transactional
    public void deactivate(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setActive(false);
        productRepository.save(product);
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .description(product.getDescription())
                .unitCost(product.getUnitCost())
                .unitPrice(product.getUnitPrice())
                .currentStock(product.getCurrentStock())
                .active(product.getActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
