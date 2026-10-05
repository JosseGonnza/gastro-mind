package com.gastromind.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PurchasePrice debería")
class PurchasePriceTest {

    @Test
    @DisplayName("calcular el coste exacto, sin pasar por el precio por kilo redondeado")
    void shouldCalculateExactCostWithoutRoundedUnitPrice() {
        var rice = new PurchasePrice(Money.of(10.0), Quantity.of(3, UnitOfMeasure.KILOGRAM));

        assertThat(rice.costOf(Quantity.of(3, UnitOfMeasure.KILOGRAM))).isEqualByComparingTo("10");
        assertThat(rice.costOf(Quantity.of(1, UnitOfMeasure.KILOGRAM))).isGreaterThan(new BigDecimal("3.33"));
    }

    @Test
    @DisplayName("calcular el coste de una cantidad en otra unidad de la misma magnitud")
    void shouldCalculateCostOfQuantityInAnotherUnit() {
        var prawns = new PurchasePrice(Money.of(37.5), Quantity.of(2.5, UnitOfMeasure.KILOGRAM));

        assertThat(prawns.costOf(Quantity.of(200, UnitOfMeasure.GRAM))).isEqualByComparingTo("3");
    }

    @Test
    @DisplayName("dar el precio por unidad del producto, para enseñarlo")
    void shouldGivePricePerUnit() {
        var prawns = new PurchasePrice(Money.of(37.5), Quantity.of(2.5, UnitOfMeasure.KILOGRAM));

        assertThat(prawns.perUnit()).isEqualTo(Money.of(15.0));
        assertThat(prawns.quantity().unit()).isEqualTo(UnitOfMeasure.KILOGRAM);
    }

    @Test
    @DisplayName("no permitir una compra de cantidad cero")
    void shouldThrowExceptionWhenQuantityIsZero() {
        assertThatThrownBy(() -> new PurchasePrice(Money.of(10.0), Quantity.zero(UnitOfMeasure.KILOGRAM)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Purchased quantity must be greater than zero");
    }

    @Test
    @DisplayName("no permitir que falte lo pagado o la cantidad")
    void shouldThrowExceptionWhenSomethingIsMissing() {
        assertThatThrownBy(() -> new PurchasePrice(null, Quantity.of(1, UnitOfMeasure.KILOGRAM)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Paid amount cannot be null");
        assertThatThrownBy(() -> new PurchasePrice(Money.of(10.0), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Purchased quantity cannot be null");
    }
}
