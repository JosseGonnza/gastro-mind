package com.gastromind.domain.valueobject;

import com.gastromind.domain.entity.Product;

public record RecipeIngredient(Product product, Quantity quantity) {

    public RecipeIngredient {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (quantity == null) {
            throw new IllegalArgumentException("Quantity cannot be null");
        }
    }

    public static RecipeIngredient of(Product product, Quantity quantity) {
        return new RecipeIngredient(product, quantity);
    }
}
