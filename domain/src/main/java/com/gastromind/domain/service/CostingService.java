package com.gastromind.domain.service;

import com.gastromind.domain.entity.Batch;
import com.gastromind.domain.entity.Product;
import com.gastromind.domain.exception.NotEnoughStockException;
import com.gastromind.domain.valueobject.Money;
import com.gastromind.domain.valueobject.Quantity;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;

public class CostingService {
    public Money calculateIngredientCost(Product product, Quantity quantity, List<Batch> batches) {
        validateIngredientsCostInputs(product, quantity, batches);

        Quantity availableStock = calculateAvailableStock(batches);
        if (!availableStock.hasEnough(quantity)) {
            throw new NotEnoughStockException(product, quantity, availableStock);
        }

        List<Batch> sortedBatches = getSortedBatches(batches);

        return calculateWeightedCost(quantity, sortedBatches);
    }

    private static List<Batch> getSortedBatches(List<Batch> batches) {
        return batches.stream()
                .filter(batch -> batch.getCurrentQuantity().value() > 0)
                .sorted(Comparator.comparing(Batch::getExpirationDate))
                .toList();
    }

    private static Money calculateWeightedCost(Quantity quantity, List<Batch> sortedBatches) {
        BigDecimal totalAccumulatedCost = BigDecimal.ZERO;
        double remainingNeeded = quantity.value();
        Currency currency = sortedBatches.getFirst().getPurchasePrice().currency();

        for (Batch batch : sortedBatches) {
            if (remainingNeeded <= 0) break;

            double batchCurrentQuantity = batch.getCurrentQuantity().value();
            // Determinamos cuánto cogemos de este lote: lo que necesitamos O lo que hay (el menor de los dos)
            double amountToTake = Math.min(remainingNeeded, batchCurrentQuantity);
            BigDecimal batchUnitCost = batch.getUnitCost().amount();
            BigDecimal chunkCost = batchUnitCost.multiply(BigDecimal.valueOf(amountToTake));

            totalAccumulatedCost = totalAccumulatedCost.add(chunkCost);
            remainingNeeded -= amountToTake;
        }
        return new Money(totalAccumulatedCost, currency);
    }

    private static Quantity calculateAvailableStock(List<Batch> batches) {
        return Quantity.of(batches.stream()
                .mapToDouble(batch -> batch.getCurrentQuantity().value())
                .sum());
    }

    private static void validateIngredientsCostInputs(Product product, Quantity quantity, List<Batch> batches) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (quantity == null) {
            throw new IllegalArgumentException("Quantity cannot be null");
        }
        if (quantity.value() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (batches == null) {
            throw new IllegalArgumentException("Batches list cannot be null");
        }
    }
}
