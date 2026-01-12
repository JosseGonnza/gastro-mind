package com.gastromind.domain.valueobject;

public enum UnitOfMeasure {
    // Masa
    KILOGRAM("kg"),
    GRAM("g"),      //Necesario para especias/repostería

    // Volumen
    LITER("L"),
    MILLILITER("ml"), //Necesario para esencias/licores

    // Unidades
    UNIT("ud"),     //Huevos, Latas, Piezas de fruta
    BUNCH("manojo"), //Perejil, Hierbabuena
    PORTION("rac");  //Útil para pre-elaboraciones

    private final String symbol;

    UnitOfMeasure(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
