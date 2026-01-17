package com.gastromind.domain.entity;

//TODO: cambiar visibilidad de los test jeje

import com.gastromind.domain.valueobject.Difficulty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Recipe debería")
public class RecipeTest {

    private static final String VALID_NAME = "Paella Valenciana";
    private static final String VALID_DESCRIPTION = "Receta tradicional valenciana";
    private static final Duration VALID_TIME = Duration.ofMinutes(45);
    private static final Difficulty VALID_DIFFICULT = Difficulty.MEDIUM;
    private static final int VALID_PORTIONS = 4;

    @Test
    @DisplayName("Crear una receta válidad vacía")
    void shouldCreateValidRecipe() {
        var recipe = Recipe.create(
                VALID_NAME,
                VALID_DESCRIPTION,
                VALID_TIME,
                VALID_DIFFICULT,
                VALID_PORTIONS
        );

        assertThat(recipe.getId()).isNotNull();
        assertThat(recipe.getName()).isEqualTo(VALID_NAME);
        assertThat(recipe.getDescription()).isEqualTo(VALID_DESCRIPTION);
        assertThat(recipe.getCookingTime()).isEqualTo(VALID_TIME);
        assertThat(recipe.getDifficult()).isEqualTo(VALID_DIFFICULT);
        assertThat(recipe.getPortions()).isEqualTo(VALID_PORTIONS);
        assertThat(recipe.getIngredients()).isEmpty();
        assertThat(recipe.getSteps()).isEmpty();
    }

    @Nested
    @DisplayName("Invariantes")
    class validations {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t", "\n"})
        @DisplayName("No permitir nombre vacío")
        void shouldThrowExceptionWhenNameIsEmpty(String invalidName) {
            assertThatThrownBy(() -> Recipe.create(
                    invalidName, VALID_DESCRIPTION, VALID_TIME, VALID_DIFFICULT, VALID_PORTIONS))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Name cannot be empty");
        }
    }
}
