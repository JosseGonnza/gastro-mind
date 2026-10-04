package com.gastromind.domain.valueobject;

import com.gastromind.domain.exception.DomainValidationException;

public record RecipeStep(int stepNumber, String description) {

    public RecipeStep {
        if (stepNumber <= 0) {
            throw new DomainValidationException("Step number must be positive");
        }
        if (description == null || description.isBlank()) {
            throw new DomainValidationException("Description cannot be empty");
        }
    }

    public static RecipeStep of(int stepNumber, String description) {
        return new RecipeStep(stepNumber, description);
    }
}
