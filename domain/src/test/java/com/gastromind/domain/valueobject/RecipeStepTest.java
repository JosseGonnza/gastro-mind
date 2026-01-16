package com.gastromind.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RecipeStep debería")
class RecipeStepTest {

    @Test
    @DisplayName("Crear un paso válido")
    void shouldCreateValidStep() {
        int stepNumber = 1;
        String description = "Calentamos el aceite";

        var step = new RecipeStep(stepNumber, description);

        assertThat(step.getStepNumbre()).isEqualTo(stepNumber);
        assertThat(step.getDescription()).isEqualTo(description);
    }
}
