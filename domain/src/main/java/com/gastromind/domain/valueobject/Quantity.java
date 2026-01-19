package com.gastromind.domain.valueobject;

public record Quantity(double value) {

    public Quantity {
        if (value < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
    }

    //Es redundante, pero mantiene la consistencia. Todos los VO se crean de la misma manera
    public static Quantity of(double value) {
        return new Quantity(value);
    }

    public Quantity add(Quantity other) {
        return new Quantity(this.value + other.value);
    }

    public Quantity subtract(Quantity other) {
        double result = this.value - other.value;
        if (result < 0) {
            throw new IllegalArgumentException("Not enough quantity available");
        }
        return new Quantity(result);
    }

    public boolean hasEnough(Quantity required) {
        return this.value >= required.value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
