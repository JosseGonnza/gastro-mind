package com.gastromind.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Money debería")
class MoneyTest {

    @Test
    @DisplayName("Crear dinero redondeando a 2 decimales")
    void shouldCreateMoney() {
        //Given
        var value = new BigDecimal("10.5555");
        //When
        var money = Money.of(value);
        //Then
        assertThat(money.amount()).isEqualTo(new BigDecimal("10.56"));
        assertThat(money.currency()).isEqualTo(Currency.getInstance("EUR"));
    }

    @Test
    @DisplayName("No permitir dinero negativo")
    void shouldThrowExceptionWhenAmountIsNegative() {
        assertThatThrownBy(() -> Money.of(-10.00))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount cannot be negative");
    }

    @Test
    @DisplayName("Sumar dinero a la misma moneda")
    void shouldAddMoneyWithSameCurrency() {
        var money1 = Money.of(10.00);
        var money2 = Money.of(5.50);

        var result = money1.add(money2);

        assertThat(result.amount()).isEqualTo(new BigDecimal("15.50"));
    }

    @Test
    @DisplayName("No permitir sumar monedas distintas")
    void shouldFailAddingDifferentCurrencies() {
        var euros = Money.of(10.00);
        var dollars = new Money(BigDecimal.TEN, Currency.getInstance("USD"));

        assertThatThrownBy(() -> euros.add(dollars))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot add different currencies");
    }

    @Test
    @DisplayName("Multiplicar correctamente")
    void shouldMultiplyMoney() {
        var price = Money.of(10.00);
        var result = price.multiply(3); // 3 unidades

        assertThat(result.amount()).isEqualTo(new BigDecimal("30.00"));
    }
}