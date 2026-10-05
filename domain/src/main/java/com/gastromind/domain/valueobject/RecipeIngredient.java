package com.gastromind.domain.valueobject;

import com.gastromind.domain.entity.Product;
import com.gastromind.domain.exception.DomainValidationException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.UUID;

public record RecipeIngredient(UUID productId, Quantity quantity, BigDecimal yieldPercentage) {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    public RecipeIngredient {
        if (productId == null) {
            throw new DomainValidationException("Product cannot be null");
        }
        if (quantity == null) {
            throw new DomainValidationException("Quantity cannot be null");
        }
        if (yieldPercentage == null) {
            throw new DomainValidationException("Yield cannot be null");
        }
        if (yieldPercentage.signum() <= 0 || yieldPercentage.compareTo(ONE_HUNDRED) > 0) {
            throw new DomainValidationException("Yield must be greater than 0 and at most 100");
        }
        yieldPercentage = yieldPercentage.stripTrailingZeros();
        if (yieldPercentage.scale() < 0) {
            yieldPercentage = yieldPercentage.setScale(0);
        }
    }

    public static RecipeIngredient of(Product product, Quantity quantity) {
        return of(product, quantity, ONE_HUNDRED);
    }

    //Al añadirlo a una receta comprobamos que la cantidad se pueda medir en la unidad del producto
    public static RecipeIngredient of(Product product, Quantity quantity, BigDecimal yieldPercentage) {
        if (product == null) {
            throw new DomainValidationException("Product cannot be null");
        }
        if (quantity == null) {
            throw new DomainValidationException("Quantity cannot be null");
        }
        if (!quantity.unit().isConvertibleTo(product.getUnit())) {
            throw new DomainValidationException("Quantity unit must be compatible with product unit");
        }
        return new RecipeIngredient(product.getId(), quantity, yieldPercentage);
    }

    //La receta pide lo que va al plato (neto); se compra y se gasta lo que hay antes de limpiar (bruto)
    public Quantity grossQuantity() {
        BigDecimal gross = quantity.amount().multiply(ONE_HUNDRED).divide(yieldPercentage, MathContext.DECIMAL64);
        return Quantity.of(gross, quantity.unit());
    }
}
