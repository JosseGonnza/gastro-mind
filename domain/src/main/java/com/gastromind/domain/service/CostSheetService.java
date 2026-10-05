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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CostSheetService {

    //IVA de hostelería en España
    private static final BigDecimal VAT_MULTIPLIER = new BigDecimal("1.10");
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    public CostSheet calculate(Recipe recipe, Collection<Product> products, Map<UUID, PurchasePrice> lastPurchases) {
        Map<UUID, Product> productsById = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity(), (first, duplicate) -> first));
        List<CostSheetLine> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        boolean complete = true;
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            Product product = productsById.get(ingredient.productId());
            if (product == null) {
                throw new DomainValidationException("Missing product for ingredient " + ingredient.productId());
            }
            PurchasePrice lastPurchase = lastPurchases.get(product.getId());
            Money cost = null;
            if (lastPurchase == null) {
                complete = false;
            } else {
                BigDecimal exactCost = lastPurchase.costOf(ingredient.grossQuantity());
                total = total.add(exactCost);
                cost = Money.of(exactCost);
            }
            lines.add(new CostSheetLine(product.getId(), product.getName(), ingredient.quantity(),
                    ingredient.yieldPercentage(), ingredient.grossQuantity(), lastPurchase, cost));
        }
        BigDecimal perPortion = total.divide(BigDecimal.valueOf(recipe.getPortions()), MathContext.DECIMAL64);
        Money salePrice = recipe.getSalePrice();
        if (salePrice == null) {
            return new CostSheet(lines, Money.of(total), Money.of(perPortion), complete, null, null, null, null);
        }
        BigDecimal withoutVat = salePrice.amount().divide(VAT_MULTIPLIER, MathContext.DECIMAL64);
        BigDecimal foodCost = perPortion.multiply(ONE_HUNDRED).divide(withoutVat, 1, RoundingMode.HALF_EVEN);
        BigDecimal margin = withoutVat.subtract(perPortion).setScale(2, RoundingMode.HALF_EVEN);
        return new CostSheet(lines, Money.of(total), Money.of(perPortion), complete,
                salePrice, Money.of(withoutVat), foodCost, margin);
    }
}
