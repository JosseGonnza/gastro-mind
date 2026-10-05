package com.gastromind.domain.valueobject;

import com.gastromind.domain.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RecipeIngredient debería")
class RecipeIngredientTest {

    private static final Product rice = Product.create(
            "Arroz Bomba",
            "Especial para paellas",
            Category.GRAIN,
            UnitOfMeasure.GRAM,
            Set.of()
    );

    private static final Product hake = Product.create(
            "Merluza entera",
            null,
            Category.FISH,
            UnitOfMeasure.KILOGRAM,
            Set.of(Allergen.FISH)
    );

    @Test
    @DisplayName("Crear un ingrediente válido que guarda solo el id del producto")
    void shouldOfValidIngredient() {
        var ingredient = RecipeIngredient.of(rice, Quantity.of(400, UnitOfMeasure.GRAM));

        assertThat(ingredient.productId()).isEqualTo(rice.getId());
        assertThat(ingredient.quantity()).isEqualTo(Quantity.of(400, UnitOfMeasure.GRAM));
    }

    @Test
    @DisplayName("No permitir un producto nulo")
    void shouldThrowExceptionWhenProductIsNull() {
        assertThatThrownBy(() -> RecipeIngredient.of(null, Quantity.of(400, UnitOfMeasure.GRAM)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product cannot be null");
    }

    //Solo validamos que venga nulo, que sea una cantidad válida se encarga el constructor de quantity
    @Test
    @DisplayName("No permitir cantidad nula")
    void shouldThrowExceptionWhenQuantityIsNull() {
        assertThatThrownBy(() -> RecipeIngredient.of(rice, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity cannot be null");
    }

    @Test
    @DisplayName("Aceptar una cantidad en otra unidad de la misma magnitud")
    void shouldAcceptQuantityInCompatibleUnit() {
        var ingredient = RecipeIngredient.of(rice, Quantity.of(0.4, UnitOfMeasure.KILOGRAM));

        assertThat(ingredient.quantity()).isEqualTo(Quantity.of(0.4, UnitOfMeasure.KILOGRAM));
    }

    @Test
    @DisplayName("Rendir al 100 % si no se indica otra cosa, sin cambiar la cantidad")
    void shouldYieldOneHundredPercentByDefault() {
        var ingredient = RecipeIngredient.of(rice, Quantity.of(400, UnitOfMeasure.GRAM));

        assertThat(ingredient.yieldPercentage()).isEqualByComparingTo("100");
        assertThat(ingredient.grossQuantity()).isEqualTo(Quantity.of(400, UnitOfMeasure.GRAM));
    }

    @Test
    @DisplayName("Calcular la cantidad bruta que hay que comprar según el rendimiento")
    void shouldCalculateGrossQuantityFromYield() {
        var halfYield = RecipeIngredient.of(hake, Quantity.of(500, UnitOfMeasure.GRAM), new BigDecimal("50"));
        var cleanedHake = RecipeIngredient.of(hake, Quantity.of(200, UnitOfMeasure.GRAM), new BigDecimal("55"));

        assertThat(halfYield.grossQuantity()).isEqualTo(Quantity.of(1000, UnitOfMeasure.GRAM));
        assertThat(cleanedHake.grossQuantity().unit()).isEqualTo(UnitOfMeasure.GRAM);
        assertThat(cleanedHake.grossQuantity().amount().setScale(2, RoundingMode.HALF_EVEN)).isEqualByComparingTo("363.64");
    }

    @Test
    @DisplayName("Considerar iguales 55 % y 55,0 %")
    void shouldIgnoreTrailingZerosInYield() {
        var ingredient = RecipeIngredient.of(hake, Quantity.of(200, UnitOfMeasure.GRAM), new BigDecimal("55"));

        assertThat(RecipeIngredient.of(hake, Quantity.of(200, UnitOfMeasure.GRAM), new BigDecimal("55.0"))).isEqualTo(ingredient);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-5", "100.01", "150"})
    @DisplayName("No permitir un rendimiento de 0 % o menos, ni de más del 100 %")
    void shouldThrowExceptionWhenYieldIsOutOfRange(String invalidYield) {
        assertThatThrownBy(() -> RecipeIngredient.of(hake, Quantity.of(200, UnitOfMeasure.GRAM), new BigDecimal(invalidYield)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Yield must be greater than 0 and at most 100");
    }

    @Test
    @DisplayName("No permitir un rendimiento nulo")
    void shouldThrowExceptionWhenYieldIsNull() {
        assertThatThrownBy(() -> RecipeIngredient.of(hake, Quantity.of(200, UnitOfMeasure.GRAM), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Yield cannot be null");
    }

    @Test
    @DisplayName("No permitir una cantidad en una unidad que no casa con la del producto")
    void shouldThrowExceptionWhenQuantityUnitIsIncompatible() {
        assertThatThrownBy(() -> RecipeIngredient.of(rice, Quantity.of(2, UnitOfMeasure.BUNCH)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity unit must be compatible with product unit");
    }
}
