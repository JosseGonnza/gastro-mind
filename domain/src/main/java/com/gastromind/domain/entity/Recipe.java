package com.gastromind.domain.entity;

import com.gastromind.domain.valueobject.Difficulty;
import com.gastromind.domain.valueobject.RecipeIngredient;
import com.gastromind.domain.valueobject.RecipeStep;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Recipe {

    private final UUID id;
    private final String name;
    private final String description;
    private final Duration cookingTime;
    private final Difficulty difficult;
    private final int portions;
    private final List<RecipeIngredient> ingredients;
    private final List<RecipeStep> steps;

    private Recipe(UUID id, String name, String description, Duration cookingTime, Difficulty difficult, int portions) {
        validateInvariants(name, cookingTime, difficult, portions);
        this.id = id;
        this.name = name;
        this.description = description;
        this.cookingTime = cookingTime;
        this.difficult = difficult;
        this.portions = portions;
        this.ingredients = new ArrayList<>();
        this.steps = new ArrayList<>();
    }

    public static Recipe create(String name, String description, Duration cookingTime,
                                Difficulty difficult, int portions) {
        return new Recipe(
                UUID.randomUUID(),
                name,
                description,
                cookingTime,
                difficult,
                portions);
    }

    public void addIngredient(RecipeIngredient ingredient) {
        if (ingredient == null) {
            throw new IllegalArgumentException("Ingredient cannot be null");
        }
        if (isAnyMatch(ingredient)) {
            throw new IllegalArgumentException("Product already exists in recipe");
        }
        ingredients.add(ingredient);
    }

    public void addStep(RecipeStep step) {
        if (step == null) {
            throw new IllegalArgumentException("Step cannot be null");
        }
        steps.add(step);
    }

    private boolean isAnyMatch(RecipeIngredient ingredient) {
        return ingredients.stream().anyMatch(i -> i.product().equals(ingredient.product()));
    }

    private static void validateInvariants(String name, Duration cookingTime, Difficulty difficult, int portions) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (cookingTime == null) {
            throw new IllegalArgumentException("Cooking time cannot be null");
        }
        if (difficult == null) {
            throw new IllegalArgumentException("Difficulty cannot be null");
        }
        if (portions <= 0) {
            throw new IllegalArgumentException("Portions must be greater than zero");
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

    public Duration getCookingTime() {
        return cookingTime;
    }

    public Difficulty getDifficult() {
        return difficult;
    }

    public int getPortions() {
        return portions;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    public List<RecipeStep> getSteps() {
        return steps;
    }
}
