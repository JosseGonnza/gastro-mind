package com.gastromind.domain.valueobject;

import com.gastromind.domain.exception.DomainValidationException;

import java.math.BigDecimal;

public record Quantity(BigDecimal amount, UnitOfMeasure unit) {

    public Quantity {
        if (amount == null) {
            throw new DomainValidationException("Amount cannot be null");
        }
        if (unit == null) {
            throw new DomainValidationException("Unit cannot be null");
        }
        if (amount.signum() < 0) {
            throw new DomainValidationException("Quantity cannot be negative");
        }
        //Sin ceros sobrantes, 2.50 kg y 2.5 kg son la misma cantidad; y 50 no se queda en 5E+1
        amount = amount.stripTrailingZeros();
        if (amount.scale() < 0) {
            amount = amount.setScale(0);
        }
    }

    public static Quantity of(BigDecimal amount, UnitOfMeasure unit) {
        return new Quantity(amount, unit);
    }

    public static Quantity of(double amount, UnitOfMeasure unit) {
        return new Quantity(BigDecimal.valueOf(amount), unit);
    }

    public static Quantity zero(UnitOfMeasure unit) {
        return new Quantity(BigDecimal.ZERO, unit);
    }

    public Quantity to(UnitOfMeasure target) {
        return new Quantity(unit.convert(amount, target), target);
    }

    public Quantity add(Quantity other) {
        return new Quantity(amount.add(other.to(unit).amount), unit);
    }

    public Quantity subtract(Quantity other) {
        BigDecimal result = amount.subtract(other.to(unit).amount);
        if (result.signum() < 0) {
            throw new DomainValidationException("Not enough quantity available");
        }
        return new Quantity(result, unit);
    }

    public boolean hasEnough(Quantity required) {
        return amount.compareTo(required.to(unit).amount) >= 0;
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    @Override
    public String toString() {
        return amount.toPlainString() + " " + unit.getSymbol();
    }
}
