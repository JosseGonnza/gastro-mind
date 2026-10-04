package com.gastromind.domain.service;

import com.gastromind.domain.entity.Batch;
import com.gastromind.domain.entity.Product;
import com.gastromind.domain.exception.NotEnoughStockException;
import com.gastromind.domain.valueobject.Quantity;

import java.util.Comparator;
import java.util.List;

public class InventoryService {

    public Quantity calculateCurrentStock(Product product, List<Batch> batches) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (batches == null || batches.isEmpty()) {
            return Quantity.zero(product.getUnit());
        }
        return batches.stream()
                .map(Batch::getCurrentQuantity)
                .reduce(Quantity.zero(product.getUnit()), Quantity::add);
    }

    //Con synchronized aseguramos la atomicidad y que solo un hilo ejecute el bloque a la vez
    public synchronized void consumeProduct(Product product, Quantity amountToConsume, List<Batch> batches) {
        validateConsumeInputs(product, amountToConsume, batches);
        /*
        TODO: Interesante para mejorar rendimiento -> ReentrantLocK
        Lo usamos por producto, por lo que no bloquea el inventario, si no el producto a consumir.
        Mejora para cuando haya mucha concurrencia!
         */
        Quantity availableToConsume = calculateCurrentStock(product, batches);
        if (!availableToConsume.hasEnough(amountToConsume)) {
            throw new NotEnoughStockException(product, amountToConsume, availableToConsume);
        }
        List<Batch> sortedBatches = batches.stream()
                .filter(batch -> !batch.getCurrentQuantity().isZero())
                .sorted(Comparator.comparing(Batch::getExpirationDate))
                .toList();
        Quantity remainingToConsume = amountToConsume;
        for (Batch batch : sortedBatches) {
            if (remainingToConsume.isZero()) {
                break;
            }
            Quantity batchStock = batch.getCurrentQuantity();
            if (batchStock.hasEnough(remainingToConsume)) {
                batch.consume(remainingToConsume);
                remainingToConsume = Quantity.zero(remainingToConsume.unit());
            } else {
                batch.consume(batchStock);
                remainingToConsume = remainingToConsume.subtract(batchStock);
            }
        }
    }

    private static void validateConsumeInputs(Product product, Quantity amountToConsume, List<Batch> batches) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (amountToConsume == null) {
            throw new IllegalArgumentException("Amount to consume cannot be null");
        }
        if (amountToConsume.isZero()) {
            throw new IllegalArgumentException("Amount to consume must be greater than zero");
        }
        if (batches == null) {
            throw new IllegalArgumentException("Batches list cannot be null");
        }
    }
}
