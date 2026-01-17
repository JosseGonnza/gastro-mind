package com.gastromind.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RecipeStep debería")
class RecipeStepTest {

    @Test
    @DisplayName("Crear un paso válido")
    void shouldCreateValidStep() {
        int stepNumber = 1;
        String description = "Calentamos el aceite";

        var step = RecipeStep.of(stepNumber, description);

        assertThat(step.stepNumber()).isEqualTo(stepNumber);
        assertThat(step.description()).isEqualTo(description);
    }

    @Test
    @DisplayName("No permitir un número de paso menos que 1")
    void shouldThrowExceptionWhenStepIsInvalid() {
        assertThatThrownBy(() -> RecipeStep.of(0, "Calentamos el aceite"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Step number must be positive");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("No permitir una descripción vacía")
    void shouldThrowExceptionWhenDescriptionIsEmpty(String invalidDescription) {
        assertThatThrownBy(() -> RecipeStep.of(1, invalidDescription))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Description cannot be empty");
    }
}
