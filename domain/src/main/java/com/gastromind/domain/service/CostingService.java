package com.gastromind.domain.service;

import com.gastromind.domain.entity.Batch;
import com.gastromind.domain.entity.Product;
import com.gastromind.domain.entity.Recipe;
import com.gastromind.domain.exception.NotEnoughStockException;
import com.gastromind.domain.valueobject.Money;
import com.gastromind.domain.valueobject.Quantity;
import com.gastromind.domain.valueobject.RecipeIngredient;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public Money calculateRecipeCost(Recipe recipe, Collection<Product> products, List<Batch> batches) {
        Map<UUID, Product> productsById = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity(), (first, duplicate) -> first));
        Money totalCost = Money.of(0.0);
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            Product product = productsById.get(ingredient.productId());
            if (product == null) {
                throw new IllegalArgumentException("Missing product for ingredient " + ingredient.productId());
            }
            List<Batch> productBatches = batches.stream()
                    .filter(batch -> batch.belongsTo(product))
                    .toList();

            Money ingredientCost = calculateIngredientCost(product, ingredient.quantity(), productBatches);
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
        if (!quantity.unit().isConvertibleTo(product.getUnit())) {
            throw new IllegalArgumentException("Quantity unit must be compatible with product unit");
        }
        if (batches == null) {
            throw new IllegalArgumentException("Batches list cannot be null");
        }
        if (batches.stream().anyMatch(batch -> !batch.belongsTo(product))) {
            throw new IllegalArgumentException("All batches must belong to the product");
        }
    }
}
