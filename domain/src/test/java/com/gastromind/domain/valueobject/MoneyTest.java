package com.gastromind.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyTest {

    @Test
    @DisplayName("Debería crear dinero redondeando a 2 decimales")
    void shouldCreateMoney() {
        //Given
        var value = new BigDecimal("10.5555");
        //When
        var money = Money.of(value);
        //Then
        assertThat(money.amount()).isEqualTo(new BigDecimal("10.56"));
        assertThat(money.currency()).isEqualTo(Currency.getInstance("EUR"));
    }
}