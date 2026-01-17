package com.gastromind.domain.valueobject;

import com.gastromind.domain.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RecipeIngredient debería")
public class RecipeIngredientTest {

    private static final Product rice = Product.create(
            "Arroz Bomba",
            "Especial para paellas",
            Category.GRAIN,
            UnitOfMeasure.GRAM,
            Set.of()
    );

    @Test
    @DisplayName("Crear un ingrediente válido")
    void shouldOfValidIngredient() {
        var ingredient = RecipeIngredient.of(rice, Quantity.of(400));

        assertThat(ingredient.product()).isEqualTo(rice);
        assertThat(ingredient.quantity().value()).isEqualTo(400);
    }

    @Test
    @DisplayName("No permitir un producto nulo")
    void shouldThrowExceptionWhenProductIsNull() {
        assertThatThrownBy(() -> RecipeIngredient.of(null, Quantity.of(400)))
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
}
