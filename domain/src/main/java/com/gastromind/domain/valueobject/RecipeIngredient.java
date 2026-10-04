package com.gastromind.domain.valueobject;

import com.gastromind.domain.entity.Product;
import com.gastromind.domain.exception.DomainValidationException;

import java.util.UUID;

public record RecipeIngredient(UUID productId, Quantity quantity) {

    public RecipeIngredient {
        if (productId == null) {
            throw new DomainValidationException("Product cannot be null");
        }
        if (quantity == null) {
            throw new DomainValidationException("Quantity cannot be null");
        }
    }

    //Al añadirlo a una receta comprobamos que la cantidad se pueda medir en la unidad del producto
    public static RecipeIngredient of(Product product, Quantity quantity) {
        if (product == null) {
            throw new DomainValidationException("Product cannot be null");
        }
        if (quantity == null) {
            throw new DomainValidationException("Quantity cannot be null");
        }
        if (!quantity.unit().isConvertibleTo(product.getUnit())) {
            throw new DomainValidationException("Quantity unit must be compatible with product unit");
        }
        return new RecipeIngredient(product.getId(), quantity);
    }
}
