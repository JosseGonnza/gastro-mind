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
        Quantity availableQuantity = Quantity.of(batches.stream()
                .mapToDouble(batch -> batch.getCurrentQuantity().value())
                .sum());
        if (!availableQuantity.hasEnough(quantity)) {
            throw new NotEnoughStockException(product, quantity, availableQuantity);
        }

        List<Batch> sortedBatches = batches.stream()
                .filter(batch -> batch.getCurrentQuantity().value() > 0)
                .sorted(Comparator.comparing(Batch::getExpirationDate))
                .toList();

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
}
