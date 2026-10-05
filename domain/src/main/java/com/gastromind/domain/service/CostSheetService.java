package com.gastromind.domain.service;

import com.gastromind.domain.entity.Product;
import com.gastromind.domain.entity.Recipe;
import com.gastromind.domain.exception.DomainValidationException;
import com.gastromind.domain.valueobject.CostSheet;
import com.gastromind.domain.valueobject.CostSheetLine;
import com.gastromind.domain.valueobject.Money;
import com.gastromind.domain.valueobject.PurchasePrice;
import com.gastromind.domain.valueobject.RecipeIngredient;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CostSheetService {

    public CostSheet calculate(Recipe recipe, Collection<Product> products, Map<UUID, PurchasePrice> lastPurchases) {
        Map<UUID, Product> productsById = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity(), (first, duplicate) -> first));
        List<CostSheetLine> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            Product product = productsById.get(ingredient.productId());
            if (product == null) {
                throw new DomainValidationException("Missing product for ingredient " + ingredient.productId());
            }
            BigDecimal cost = lastPurchases.get(product.getId()).costOf(ingredient.grossQuantity());
            total = total.add(cost);
            lines.add(new CostSheetLine(product.getId(), product.getName(), ingredient.quantity(),
                    ingredient.yieldPercentage(), ingredient.grossQuantity(), Money.of(cost)));
        }
        BigDecimal perPortion = total.divide(BigDecimal.valueOf(recipe.getPortions()), MathContext.DECIMAL64);
        return new CostSheet(lines, Money.of(total), Money.of(perPortion));
    }
}
