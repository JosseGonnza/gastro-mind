package com.gastromind.application.product;

import com.gastromind.domain.valueobject.Allergen;
import com.gastromind.domain.valueobject.Category;
import com.gastromind.domain.valueobject.UnitOfMeasure;

import java.util.Set;

public record CreateProductCommand(
        String name,
        String description,
        Category category,
        UnitOfMeasure unit,
        Set<Allergen> allergens
) {
}
