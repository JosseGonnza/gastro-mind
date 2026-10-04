package com.gastromind.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static com.gastromind.domain.valueobject.UnitOfMeasure.GRAM;
import static com.gastromind.domain.valueobject.UnitOfMeasure.KILOGRAM;
import static com.gastromind.domain.valueobject.UnitOfMeasure.LITER;
import static com.gastromind.domain.valueobject.UnitOfMeasure.MILLILITER;
import static com.gastromind.domain.valueobject.UnitOfMeasure.UNIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Quantity debería")
class QuantityTest {

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 1.0, 500.0})
    @DisplayName("Crear una cantidad válida")
    void shouldCreateValidQuantity(double validQuantity) {
        var quantity = Quantity.of(validQuantity, GRAM);

        assertThat(quantity.amount()).isEqualByComparingTo(BigDecimal.valueOf(validQuantity));
        assertThat(quantity.unit()).isEqualTo(GRAM);
    }

    @Test
    @DisplayName("No permitir cantidades negativas")
    void shouldThrowExceptionWhenQuantityIsNegative() {
        assertThatThrownBy(() -> Quantity.of(-1.0, GRAM))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity cannot be negative");
    }

    @Test
    @DisplayName("No permitir cantidades sin unidad")
    void shouldThrowExceptionWhenUnitIsNull() {
        assertThatThrownBy(() -> Quantity.of(1.0, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unit cannot be null");
    }

    @Test
    @DisplayName("Sumar cantidades")
    void shouldAddQuantities() {
        var quantity1 = Quantity.of(100.0, GRAM);
        var quantity2 = Quantity.of(50.0, GRAM);

        var result = quantity1.add(quantity2);

        assertThat(result).isEqualTo(Quantity.of(150, GRAM));
    }

    @Test
    @DisplayName("Restar cantidades si hay suficiente")
    void shouldSubtractQuantities() {
        var stock = Quantity.of(100.0, GRAM);
        var used = Quantity.of(50.0, GRAM);

        var result = stock.subtract(used);

        assertThat(result).isEqualTo(Quantity.of(50, GRAM));
    }

    @Test
    @DisplayName("No permitir restar más de lo que hay disponible")
    void shouldThrowExceptionWhenSubtractingMoreThanAvailable() {
        var stock = Quantity.of(50.0, GRAM);
        var required = Quantity.of(100.0, GRAM);

        assertThatThrownBy(() -> stock.subtract(required))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not enough quantity available");
    }

    @Test
    @DisplayName("Verificar si hay suficiente stock")
    void shouldCheckIfQuantityIsEnough() {
        var stock = Quantity.of(500.0, GRAM);

        assertThat(stock.hasEnough(Quantity.of(400.0, GRAM))).isTrue();
        assertThat(stock.hasEnough(Quantity.of(600.0, GRAM))).isFalse();
    }

    @Test
    @DisplayName("Convertir entre unidades de la misma magnitud")
    void shouldConvertBetweenCompatibleUnits() {
        assertThat(Quantity.of(200, GRAM).to(KILOGRAM)).isEqualTo(Quantity.of(0.2, KILOGRAM));
        assertThat(Quantity.of(1.5, LITER).to(MILLILITER)).isEqualTo(Quantity.of(1500, MILLILITER));
    }

    @Test
    @DisplayName("No convertir entre unidades de distinta magnitud")
    void shouldNotConvertBetweenIncompatibleUnits() {
        assertThatThrownBy(() -> Quantity.of(2, UNIT).to(KILOGRAM))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot convert UNIT to KILOGRAM");
    }

    @Test
    @DisplayName("Sumar, restar y comparar cantidades en unidades distintas, en la unidad de la primera")
    void shouldOperateWithQuantitiesInDifferentUnits() {
        var rice = Quantity.of(1, KILOGRAM);

        assertThat(rice.add(Quantity.of(250, GRAM))).isEqualTo(Quantity.of(1.25, KILOGRAM));
        assertThat(rice.subtract(Quantity.of(200, GRAM))).isEqualTo(Quantity.of(0.8, KILOGRAM));
        assertThat(rice.hasEnough(Quantity.of(800, GRAM))).isTrue();
        assertThat(rice.hasEnough(Quantity.of(1200, GRAM))).isFalse();
    }

    @Test
    @DisplayName("Ser igual aunque se escriba con distintos decimales")
    void shouldBeEqualRegardlessOfScale() {
        assertThat(Quantity.of(new BigDecimal("2.50"), KILOGRAM)).isEqualTo(Quantity.of(2.5, KILOGRAM));
    }

    @Test
    @DisplayName("Mostrarse con su unidad")
    void shouldShowAmountWithUnit() {
        assertThat(Quantity.of(0.2, KILOGRAM)).hasToString("0.2 kg");
        assertThat(Quantity.of(50, GRAM)).hasToString("50 g");
    }

    @Test
    @DisplayName("No guardar el importe en notación científica")
    void shouldNotUseScientificNotation() {
        assertThat(Quantity.of(50, GRAM).amount().toString()).isEqualTo("50");
        assertThat(Quantity.of(new BigDecimal("1500.00"), MILLILITER).amount().toString()).isEqualTo("1500");
    }
}
