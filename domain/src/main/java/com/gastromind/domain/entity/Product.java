package com.gastromind.domain.entity;

import com.gastromind.domain.valueobject.Allergen;
import com.gastromind.domain.valueobject.Category;
import com.gastromind.domain.valueobject.UnitOfMeasure;

import java.util.Set;
import java.util.UUID;

public class Product {
    private final UUID id;
    private final String name;
    private final String description;
    private final Category category;
    private final UnitOfMeasure unit;
    private final Set<Allergen> allergens;

    public Product(UUID id, String name, String description, Category category,
                   UnitOfMeasure unit, Set<Allergen> allergens) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.unit = unit;
        this.allergens = allergens;
    }

    public static Product create(String name, String description, Category category,
                                 UnitOfMeasure unit, Set<Allergen> allergens) {
        return new Product(
                UUID.randomUUID(),
                name,
                description,
                category,
                unit,
                allergens
        );
    }

    public UUID getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public Category getCategory() {
        return category;
    }
    public UnitOfMeasure getUnit() {
        return unit;
    }
    public Set<Allergen> getAllergens() {
        return allergens;
    }
}
