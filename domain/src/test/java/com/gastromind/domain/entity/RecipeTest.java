package com.gastromind.domain.entity;

import com.gastromind.domain.valueobject.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Recipe debería")
class RecipeTest {

    private static final String VALID_NAME = "Paella Valenciana";
    private static final String VALID_DESCRIPTION = "Receta tradicional valenciana";
    private static final int VALID_PORTIONS = 4;
    private static final Money VALID_SALE_PRICE = Money.of(18.5);

    @Test
    @DisplayName("Crear una receta válidad vacía")
    void shouldCreateValidRecipe() {
        var recipe = Recipe.create(VALID_NAME, VALID_DESCRIPTION, VALID_PORTIONS, VALID_SALE_PRICE);

        assertThat(recipe.getId()).isNotNull();
        assertThat(recipe.getName()).isEqualTo(VALID_NAME);
        assertThat(recipe.getDescription()).isEqualTo(VALID_DESCRIPTION);
        assertThat(recipe.getPortions()).isEqualTo(VALID_PORTIONS);
        assertThat(recipe.getSalePrice()).isEqualTo(VALID_SALE_PRICE);
        assertThat(recipe.getIngredients()).isEmpty();
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
                    invalidName, VALID_DESCRIPTION, VALID_PORTIONS, VALID_SALE_PRICE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Name cannot be empty");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -10})
        @DisplayName("No permitir porciones menor o igual a cero")
        void shouldThrowExceptionWhenPortionIsInvalid(int invalidPortions) {
            assertThatThrownBy(() -> Recipe.create(
                    VALID_NAME, VALID_DESCRIPTION, invalidPortions, VALID_SALE_PRICE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Portions must be greater than zero");
        }
    }

    @Test
    @DisplayName("Permitir una receta que todavía no tiene precio al público")
    void shouldAllowRecipeWithoutSalePrice() {
        var recipe = Recipe.create(VALID_NAME, VALID_DESCRIPTION, VALID_PORTIONS, null);

        assertThat(recipe.getSalePrice()).isNull();
    }

    @Test
    @DisplayName("No permitir un precio al público de 0 €")
    void shouldThrowExceptionWhenSalePriceIsZero() {
        assertThatThrownBy(() -> Recipe.create(VALID_NAME, VALID_DESCRIPTION, VALID_PORTIONS, Money.of(0.0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Sale price must be greater than zero");
    }

    @Test
    @DisplayName("No dejar modificar los ingredientes desde fuera, saltándose sus validaciones")
    void shouldNotExposeMutableIngredients() {
        var recipe = Recipe.create(VALID_NAME, VALID_DESCRIPTION, VALID_PORTIONS, VALID_SALE_PRICE);
        var rice = Product.create("Arroz Bomba", "Especial para paellas", Category.GRAIN, UnitOfMeasure.GRAM, Set.of());

        assertThatThrownBy(() -> recipe.getIngredients().add(RecipeIngredient.of(rice, Quantity.of(400, UnitOfMeasure.GRAM))))
                .isInstanceOf(UnsupportedOperationException.class);
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
            var recipe = Recipe.create(VALID_NAME, VALID_DESCRIPTION, VALID_PORTIONS, VALID_SALE_PRICE);
            var ingredient = RecipeIngredient.of(rice, Quantity.of(400, UnitOfMeasure.GRAM));

            recipe.addIngredient(ingredient);

            assertThat(recipe.getIngredients()).hasSize(1);
            assertThat(recipe.getIngredients()).contains(ingredient);
        }

        @Test
        @DisplayName("No permitir añadir un ingrediente nulo")
        void shouldThrowExceptionWhenIngredientIsNull() {
            var recipe = Recipe.create(VALID_NAME, VALID_DESCRIPTION, VALID_PORTIONS, VALID_SALE_PRICE);

            assertThatThrownBy(() -> recipe.addIngredient(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Ingredient cannot be null");
        }

        @Test
        @DisplayName("No permitir productos duplicados")
        void shouldThrowExceptionWhenProductIsDuplicated() {
            var recipe = Recipe.create(VALID_NAME, VALID_DESCRIPTION, VALID_PORTIONS, VALID_SALE_PRICE);
            var ingredient1 = RecipeIngredient.of(rice, Quantity.of(400, UnitOfMeasure.GRAM));
            var ingredient2 = RecipeIngredient.of(rice, Quantity.of(200, UnitOfMeasure.GRAM));

            recipe.addIngredient(ingredient1);

            assertThatThrownBy(() -> recipe.addIngredient(ingredient2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Product already exists in recipe");
        }

        @Test
        @DisplayName("Permitir añadir ingredientes diferentes")
        void shouldAddSeveralDifferentIngredients() {
            var recipe = Recipe.create(VALID_NAME, VALID_DESCRIPTION, VALID_PORTIONS, VALID_SALE_PRICE);
            var ingredient1 = RecipeIngredient.of(rice, Quantity.of(400, UnitOfMeasure.GRAM));
            var ingredient2 = RecipeIngredient.of(chicken, Quantity.of(200, UnitOfMeasure.GRAM));

            recipe.addIngredient(ingredient1);
            recipe.addIngredient(ingredient2);

            assertThat(recipe.getIngredients()).hasSize(2);
            assertThat(recipe.getIngredients()).contains(ingredient1);
            assertThat(recipe.getIngredients()).contains(ingredient2);
        }
    }
}
