package com.gastromind.domain.valueobject;

import com.gastromind.domain.exception.DomainValidationException;

import java.math.BigDecimal;
import java.math.MathContext;

//Lo que costó una compra tal cual ("37,50 € por 2,5 kg"): el precio por kilo redondeado a céntimos descuadra los costes
public record PurchasePrice(Money paid, Quantity quantity) {

    public PurchasePrice {
        if (paid == null) {
            throw new DomainValidationException("Paid amount cannot be null");
        }
        if (quantity == null) {
            throw new DomainValidationException("Purchased quantity cannot be null");
        }
        if (quantity.isZero()) {
            throw new DomainValidationException("Purchased quantity must be greater than zero");
        }
    }

    public BigDecimal costOf(Quantity needed) {
        return paid.amount()
                .multiply(needed.to(quantity.unit()).amount())
                .divide(quantity.amount(), MathContext.DECIMAL64);
    }

    public Money perUnit() {
        return new Money(paid.amount().divide(quantity.amount(), MathContext.DECIMAL64), paid.currency());
    }
}
