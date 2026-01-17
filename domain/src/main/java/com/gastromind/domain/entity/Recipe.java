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
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
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
