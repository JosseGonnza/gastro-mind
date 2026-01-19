package com.gastromind.domain.service;

import com.gastromind.domain.entity.Batch;
import com.gastromind.domain.entity.Product;
import com.gastromind.domain.valueobject.Quantity;

import java.util.List;

public class InventoryService {


    public Quantity calculateCurrentStock(Product product, List<Batch> bathes) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (bathes == null || bathes.isEmpty()) {
            return Quantity.of(0);
        }
        return Quantity.of(bathes.stream()
                .mapToDouble(batch -> batch.getCurrentQuantity().value())
                .sum()
        );
    }
}
