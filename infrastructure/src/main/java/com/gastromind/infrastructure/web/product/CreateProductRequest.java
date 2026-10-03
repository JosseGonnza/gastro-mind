package com.gastromind.infrastructure.web.product;

import com.gastromind.application.product.CreateProductCommand;
import com.gastromind.domain.valueobject.Allergen;
import com.gastromind.domain.valueobject.Category;
import com.gastromind.domain.valueobject.UnitOfMeasure;

import java.util.Set;

public record CreateProductRequest(
        String name,
        String description,
        Category category,
        UnitOfMeasure unit,
        Set<Allergen> allergens
) {

    public CreateProductCommand toCommand() {
        return new CreateProductCommand(name, description, category, unit, allergens);
    }
}
