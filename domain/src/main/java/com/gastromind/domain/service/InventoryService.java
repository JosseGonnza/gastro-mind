package com.gastromind.domain.service;

import com.gastromind.domain.entity.Batch;
import com.gastromind.domain.entity.Product;
import com.gastromind.domain.exception.NotEnoughStockException;
import com.gastromind.domain.valueobject.Quantity;

import java.util.ArrayList;
import java.util.Comparator;
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

    public void consumeProduct(Product product, Quantity amountToConsume, List<Batch> batches) {
        Quantity availableToConsume = calculateCurrentStock(product, batches);
        if (availableToConsume.value() < amountToConsume.value()) {
            throw new NotEnoughStockException(product, amountToConsume, availableToConsume);
        }
        List<Batch> sortedBatches = batches.stream()
                .filter(batch -> batch.getCurrentQuantity().value() > 0)
                .sorted(Comparator.comparing(Batch::getEntryDate))
                .toList();
        double remainingToConsume = amountToConsume.value();
        for (Batch batch : sortedBatches) {
            if (remainingToConsume <= 0) {
                break;
            }
            double batchStock = batch.getCurrentQuantity().value();
            if (batchStock >= remainingToConsume) {
                batch.consume(Quantity.of(remainingToConsume));
                remainingToConsume = 0;
            } else {
                batch.consume(Quantity.of(batchStock));
                remainingToConsume -= batchStock;
            }
        }
    }
}
