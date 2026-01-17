package com.gastromind.domain.entity;

import com.gastromind.domain.valueobject.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Recipe debería")
class RecipeTest {

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

        @Test
        @DisplayName("No permitir tiempo de cocinado nulo")
        void shouldThrowExceptionWhenCookingTimeIsNull() {
            assertThatThrownBy(() -> Recipe.create(
                    VALID_NAME, VALID_DESCRIPTION, null, VALID_DIFFICULT, VALID_PORTIONS))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Cooking time cannot be null");
        }

        @Test
        @DisplayName("No permitir dificultad nula")
        void shouldThrowExceptionWhenDifficultyIsNull() {
            assertThatThrownBy(() -> Recipe.create(
                    VALID_NAME, VALID_DESCRIPTION, VALID_TIME, null, VALID_PORTIONS))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Difficulty cannot be null");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -10})
        @DisplayName("No permitir porciones menor o igual a cero")
        void shouldThrowExceptionWhenPortionIsInvalid(int invalidPortions) {
            assertThatThrownBy(() -> Recipe.create(
                    VALID_NAME, VALID_DESCRIPTION, VALID_TIME, VALID_DIFFICULT, invalidPortions))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Portions must be greater than zero");
        }
    }

    @Nested
    @DisplayName("Gestión de Ingredientes")
    class IngredientManagement {

        private static final Product rice = Product.create("Arroz Bomba", "Especial para paellas",
                Category.GRAIN, UnitOfMeasure.GRAM, Set.of());
        private static final Product chicken = Product.create("Pollo", "Muslos",
                Category.MEAT, UnitOfMeasure.GRAM, Set.of());

        @Test
        @DisplayName("Permitir añadir un ingrediente correctamente")
        void shouldAddIngredient() {
            var recipe = Recipe.create(VALID_NAME, VALID_DESCRIPTION, VALID_TIME, VALID_DIFFICULT, VALID_PORTIONS);
            var ingredient = RecipeIngredient.of(rice, Quantity.of(400));

            recipe.addIngredient(ingredient);

            assertThat(recipe.getIngredients()).hasSize(1);
            assertThat(recipe.getIngredients()).contains(ingredient);
        }

        @Test
        @DisplayName("No permitir añadir un ingrediente nulo")
        void shouldThrowExceptionWhenIngredientIsNull() {
            var recipe = Recipe.create(VALID_NAME, VALID_DESCRIPTION, VALID_TIME, VALID_DIFFICULT, VALID_PORTIONS);

            assertThatThrownBy(() -> recipe.addIngredient(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Ingredient cannot be null");
        }

        @Test
        @DisplayName("No permitir productos duplicados")
        void shouldThrowExceptionWhenProductIsDuplicated() {
            var recipe = Recipe.create(VALID_NAME, VALID_DESCRIPTION, VALID_TIME, VALID_DIFFICULT, VALID_PORTIONS);
            var ingredient1 = RecipeIngredient.of(rice, Quantity.of(400));
            var ingredient2 = RecipeIngredient.of(rice, Quantity.of(200));

            recipe.addIngredient(ingredient1);

            assertThatThrownBy(() -> recipe.addIngredient(ingredient2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Product already exists in recipe");
        }

        @Test
        @DisplayName("Permitir añadir ingredientes diferentes")
        void shouldAddSeveralDifferentIngredients() {
            var recipe = Recipe.create(VALID_NAME, VALID_DESCRIPTION, VALID_TIME, VALID_DIFFICULT, VALID_PORTIONS);
            var ingredient1 = RecipeIngredient.of(rice, Quantity.of(400));
            var ingredient2 = RecipeIngredient.of(chicken, Quantity.of(200));

            recipe.addIngredient(ingredient1);
            recipe.addIngredient(ingredient2);

            assertThat(recipe.getIngredients()).hasSize(2);
            assertThat(recipe.getIngredients()).contains(ingredient1);
            assertThat(recipe.getIngredients()).contains(ingredient2);
        }
    }

    @Nested
    @DisplayName("Gestión de pasos")
    class StepManagement {

        @Test
        @DisplayName("Permitir añadir un paso correctamente")
        void shouldAddStep() {
            var recipe = Recipe.create(VALID_NAME, VALID_DESCRIPTION, VALID_TIME, VALID_DIFFICULT, VALID_PORTIONS);
            var step = RecipeStep.of(1, "Calentar el aceite");

            recipe.addStep(step);

            assertThat(recipe.getSteps()).hasSize(1);
            assertThat(recipe.getSteps()).contains(step);
        }

        @Test
        @DisplayName("No permitir añadir un paso nulo")
        void shouldExceptionWhenInvalidStep() {
            var recipe = Recipe.create(VALID_NAME, VALID_DESCRIPTION, VALID_TIME, VALID_DIFFICULT, VALID_PORTIONS);

            assertThatThrownBy(() -> recipe.addStep(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Step cannot be null");
        }

        @Test
        @DisplayName("Permitir añadir varios pasos")
        void shouldAddSeveralSteps() {
            var recipe = Recipe.create(VALID_NAME, VALID_DESCRIPTION, VALID_TIME, VALID_DIFFICULT, VALID_PORTIONS);
            var step1 = RecipeStep.of(1, "Calentar el aceite");
            var step2 = RecipeStep.of(2, "Sofreir la cebolla");

            recipe.addStep(step1);
            recipe.addStep(step2);

            assertThat(recipe.getSteps()).hasSize(2);
            assertThat(recipe.getSteps()).contains(step1);
            assertThat(recipe.getSteps()).contains(step2);
        }
    }
}
