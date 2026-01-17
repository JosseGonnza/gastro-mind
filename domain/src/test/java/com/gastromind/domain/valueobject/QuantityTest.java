package com.gastromind.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Quantity debería")
class QuantityTest {

    @Test
    @DisplayName("Crear una cantidad válida")
    void shouldCreateValidQuantity() {
        var quantity = Quantity.of(500.0);

        assertThat(quantity.value()).isEqualTo(500.0);
    }

    @Test
    @DisplayName("No permitir cantidades negativas")
    void shouldThrowExceptionWhenQuantityIsNegative() {
        assertThatThrownBy(() -> Quantity.of(-1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be greater than zero");
    }

    @Test
    @DisplayName("Sumar cantidades")
    void shouldAddQuantities() {
        var quantity1 = Quantity.of(100.0);
        var quantity2 = Quantity.of(50.0);

        var result = quantity1.add(quantity2);

        assertThat(result.value()).isEqualTo(150.0);
    }

    @Test
    @DisplayName("Restar cantidades si hay suficiente")
    void shouldSubtractQuantities() {
        var stock = Quantity.of(100.0);
        var used = Quantity.of(50.0);

        var result = stock.subtract(used);

        assertThat(result.value()).isEqualTo(50.0);
    }

    @Test
    @DisplayName("No permitir restar más de lo que hay disponible")
    void shouldThrowExceptionWhenSubtractingMoreThanAvailable() {
        var stock = Quantity.of(50.0);
        var required = Quantity.of(100.0);

        assertThatThrownBy(() -> stock.subtract(required))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not enough quantity available");
    }

    @Test
    @DisplayName("Verificar si hay suficiente stock")
    void shouldCheckIfQuantityIsEnough() {
        var stock = Quantity.of(500.0);

        assertThat(stock.hasEnough(Quantity.of(400.0))).isTrue();
        assertThat(stock.hasEnough(Quantity.of(600.0))).isFalse();
    }
}