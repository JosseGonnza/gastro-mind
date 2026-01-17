package com.gastromind.domain.valueobject;

public record RecipeStep(int stepNumber, String description) {

    public RecipeStep {
        if (stepNumber <= 0) {
            throw new IllegalArgumentException("Step number must be positive");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }

        //TODO: cambiar a un metodo de factoría RecipeStep.of() para mantener la consistencia de los VO
    }
}
