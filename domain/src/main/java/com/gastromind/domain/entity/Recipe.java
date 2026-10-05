package com.gastromind.domain.entity;

import com.gastromind.domain.exception.DomainValidationException;
import com.gastromind.domain.valueobject.RecipeIngredient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Recipe {

    private final UUID id;
    private final String name;
    private final String description;
    private final int portions;
    private final List<RecipeIngredient> ingredients;

    private Recipe(UUID id, String name, String description, int portions) {
        validateInvariants(name, portions);
        this.id = id;
        this.name = name;
        this.description = description;
        this.portions = portions;
        this.ingredients = new ArrayList<>();
    }

    public static Recipe create(String name, String description, int portions) {
        return new Recipe(UUID.randomUUID(), name, description, portions);
    }

    public void addIngredient(RecipeIngredient ingredient) {
        if (ingredient == null) {
            throw new DomainValidationException("Ingredient cannot be null");
        }
        if (isAnyMatch(ingredient)) {
            throw new DomainValidationException("Product already exists in recipe");
        }
        ingredients.add(ingredient);
    }

    private boolean isAnyMatch(RecipeIngredient ingredient) {
        return ingredients.stream().anyMatch(i -> i.productId().equals(ingredient.productId()));
    }

    private static void validateInvariants(String name, int portions) {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("Name cannot be empty");
        }
        if (portions <= 0) {
            throw new DomainValidationException("Portions must be greater than zero");
        }
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

    public int getPortions() {
        return portions;
    }

    public List<RecipeIngredient> getIngredients() {
        return Collections.unmodifiableList(ingredients);
    }
}
