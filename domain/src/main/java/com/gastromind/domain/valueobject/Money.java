package com.gastromind.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) {

    public Money {
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        //Redondeo del banquero -> Al par más cercano
        //TODO: Interesante usar "currency.getDefaultFractionDigits()" para que la moneda decida los decimales
        amount = amount.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount, Currency.getInstance("EUR"));
    }

    public static Money of(double amount) {
        return new Money(BigDecimal.valueOf(amount), Currency.getInstance("EUR"));
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add different currencies");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money multiply(int multiplier) {
        //TODO: Permitimos el 0 de momento, por si no tenemos stock de un producto que no rompa
        if (multiplier < 0) {
            throw new IllegalArgumentException("Multiplier cannot be negative");
        }
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)), this.currency);
    }
}
