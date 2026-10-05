package com.gastromind.domain.valueobject;

import java.math.BigDecimal;
import java.util.List;

//El escandallo de una receta
//complete es false si a algún producto le falta precio: entonces el coste se queda corto
//Sin precio en la carta, salePrice y lo que sale de él llegan a null. El margen va en BigDecimal porque puede ser negativo
public record CostSheet(List<CostSheetLine> lines, Money totalCost, Money costPerPortion, boolean complete,
                        Money salePrice, Money salePriceWithoutVat, BigDecimal foodCostPercentage, BigDecimal margin) {

    public CostSheet {
        lines = List.copyOf(lines);
    }
}
