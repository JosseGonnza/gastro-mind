package com.gastromind.domain.valueobject;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

//El escandallo de una receta
//complete es false si a algún producto le falta precio: entonces el coste se queda corto
//Sin precio en la carta, salePrice y lo que sale de él llegan a null. El margen va en BigDecimal porque puede ser negativo
public record CostSheet(List<CostSheetLine> lines, Money totalCost, Money costPerPortion, boolean complete,
                        Money salePrice, Money salePriceWithoutVat, BigDecimal foodCostPercentage, BigDecimal margin,
                        Set<Allergen> allergens) {

    public CostSheet {
        lines = List.copyOf(lines);
        //EnumSet para que salgan siempre en el mismo orden, el de la lista oficial
        EnumSet<Allergen> sorted = EnumSet.noneOf(Allergen.class);
        sorted.addAll(allergens);
        allergens = Collections.unmodifiableSet(sorted);
    }
}
