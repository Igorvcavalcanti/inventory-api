package com.igorcavalcanti.inventory_api.stockmovement.service;

public class InsufficientStockException extends RuntimeException{
    public InsufficientStockException(Long productId, int currentStock, int requested) {
        super("Insufficient stock for productId=" +productId +
                " (currentStock+" + currentStock + ", requested+" + requested + ")");
    }
}
