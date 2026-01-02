package com.igorcavalcanti.inventory_api.product.service;

public class ProductNotFoundException extends RuntimeException{

    public ProductNotFoundException(Long id) {
        super("Product not found with id: " + id);
    }
}
