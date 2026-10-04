package com.gastromind.domain.service;

import com.gastromind.domain.entity.Batch;
import com.gastromind.domain.entity.Product;
import com.gastromind.domain.entity.Recipe;
import com.gastromind.domain.exception.NotEnoughStockException;
import com.gastromind.domain.valueobject.Money;
import com.gastromind.domain.valueobject.Quantity;
import com.gastromind.domain.valueobject.RecipeIngredient;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Map;

public class CostingService {
    public Money calculateIngredientCost(Product product, Quantity quantity, List<Batch> batches) {
        validateIngredientsCostInputs(product, quantity, batches);

        Quantity availableStock = calculateAvailableStock(product, batches);
        if (!availableStock.hasEnough(quantity)) {
            throw new NotEnoughStockException(product, quantity, availableStock);
        }

        List<Batch> sortedBatches = getSortedBatches(batches);

        return calculateWeightedCost(quantity, sortedBatches);
    }

    public Money calculateRecipeCost(Recipe recipe, Map<Product, List<Batch>> availableBatches) {
        Money totalCost = Money.of(0.0);
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            Product product = ingredient.product();
            Quantity requiredQuantity = ingredient.quantity();

            List<Batch> batches = availableBatches.get(product);

            Money ingredientCost = calculateIngredientCost(product, requiredQuantity, batches);
            totalCost = totalCost.add(ingredientCost);
        }
        return totalCost;
    }

    private static List<Batch> getSortedBatches(List<Batch> batches) {
        return batches.stream()
                .filter(batch -> !batch.getCurrentQuantity().isZero())
                .sorted(Comparator.comparing(Batch::getExpirationDate))
                .toList();
    }

    private static Money calculateWeightedCost(Quantity quantity, List<Batch> sortedBatches) {
        BigDecimal totalAccumulatedCost = BigDecimal.ZERO;
        Quantity remainingNeeded = quantity;
        Currency currency = sortedBatches.getFirst().getPurchasePrice().currency();

        for (Batch batch : sortedBatches) {
            if (remainingNeeded.isZero()) break;

            Quantity batchCurrentQuantity = batch.getCurrentQuantity();
            // Determinamos cuánto cogemos de este lote: lo que necesitamos O lo que hay (el menor de los dos)
            Quantity amountToTake = batchCurrentQuantity.hasEnough(remainingNeeded)
                    ? remainingNeeded.to(batchCurrentQuantity.unit())
                    : batchCurrentQuantity;
            BigDecimal batchUnitCost = batch.getUnitCost().amount();
            BigDecimal chunkCost = batchUnitCost.multiply(amountToTake.amount());

            totalAccumulatedCost = totalAccumulatedCost.add(chunkCost);
            remainingNeeded = remainingNeeded.subtract(amountToTake);
        }
        return new Money(totalAccumulatedCost, currency);
    }

    private static Quantity calculateAvailableStock(Product product, List<Batch> batches) {
        return batches.stream()
                .map(Batch::getCurrentQuantity)
                .reduce(Quantity.zero(product.getUnit()), Quantity::add);
    }

    private static void validateIngredientsCostInputs(Product product, Quantity quantity, List<Batch> batches) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (quantity == null) {
            throw new IllegalArgumentException("Quantity cannot be null");
        }
        if (quantity.isZero()) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (batches == null) {
            throw new IllegalArgumentException("Batches list cannot be null");
        }
    }
}
