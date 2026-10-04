package com.gastromind.domain.valueobject;

import com.gastromind.domain.exception.DomainValidationException;

import java.math.BigDecimal;

public enum UnitOfMeasure {
    // Masa
    KILOGRAM("kg", Dimension.MASS, 1000),
    GRAM("g", Dimension.MASS, 1),      //Necesario para especias/repostería

    // Volumen
    LITER("L", Dimension.VOLUME, 1000),
    MILLILITER("ml", Dimension.VOLUME, 1), //Necesario para esencias/licores

    // Unidades
    UNIT("ud", Dimension.UNITS, 1),     //Huevos, Latas, Piezas de fruta
    BUNCH("manojo", Dimension.BUNCHES, 1), //Perejil, Hierbabuena
    PORTION("rac", Dimension.PORTIONS, 1);  //Útil para pre-elaboraciones

    //Solo se convierte dentro de la misma magnitud: un manojo no es una unidad ni una ración
    private enum Dimension { MASS, VOLUME, UNITS, BUNCHES, PORTIONS }

    private final String symbol;
    private final Dimension dimension;
    private final BigDecimal factorToBase;

    UnitOfMeasure(String symbol, Dimension dimension, int factorToBase) {
        this.symbol = symbol;
        this.dimension = dimension;
        this.factorToBase = BigDecimal.valueOf(factorToBase);
    }

    public String getSymbol() {
        return symbol;
    }

    public boolean isConvertibleTo(UnitOfMeasure target) {
        return dimension == target.dimension;
    }

    public BigDecimal convert(BigDecimal amount, UnitOfMeasure target) {
        if (!isConvertibleTo(target)) {
            throw new DomainValidationException("Cannot convert " + this + " to " + target);
        }
        return amount.multiply(factorToBase).divide(target.factorToBase);
    }
}
