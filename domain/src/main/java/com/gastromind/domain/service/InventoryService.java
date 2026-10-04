package com.gastromind.domain.service;

import com.gastromind.domain.entity.Batch;
import com.gastromind.domain.entity.Product;
import com.gastromind.domain.exception.DomainValidationException;
import com.gastromind.domain.exception.NotEnoughStockException;
import com.gastromind.domain.valueobject.Quantity;

import java.util.Comparator;
import java.util.List;

public class InventoryService {

    public Quantity calculateCurrentStock(Product product, List<Batch> batches) {
        if (product == null) {
            throw new DomainValidationException("Product cannot be null");
        }
        if (batches == null || batches.isEmpty()) {
            return Quantity.zero(product.getUnit());
        }
        if (batches.stream().anyMatch(batch -> !batch.belongsTo(product))) {
            throw new DomainValidationException("All batches must belong to the product");
        }
        return batches.stream()
                .map(Batch::getCurrentQuantity)
                .reduce(Quantity.zero(product.getUnit()), Quantity::add);
    }

    //Dos consumos a la vez sobre el mismo lote se resuelven al guardar (transacción con bloqueo de fila), no aquí
    public void consumeProduct(Product product, Quantity amountToConsume, List<Batch> batches) {
        validateConsumeInputs(product, amountToConsume, batches);
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
            throw new DomainValidationException("Product cannot be null");
        }
        if (amountToConsume == null) {
            throw new DomainValidationException("Amount to consume cannot be null");
        }
        if (amountToConsume.isZero()) {
            throw new DomainValidationException("Amount to consume must be greater than zero");
        }
        if (!amountToConsume.unit().isConvertibleTo(product.getUnit())) {
            throw new DomainValidationException("Quantity unit must be compatible with product unit");
        }
        if (batches == null) {
            throw new DomainValidationException("Batches list cannot be null");
        }
        if (batches.stream().anyMatch(batch -> !batch.belongsTo(product))) {
            throw new DomainValidationException("All batches must belong to the product");
        }
    }
}
