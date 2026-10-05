package com.gastromind.domain.valueobject;

import java.util.List;

//El escandallo de una receta
//complete es false si a algún producto le falta precio: entonces el coste se queda corto
public record CostSheet(List<CostSheetLine> lines, Money totalCost, Money costPerPortion, boolean complete) {

    public CostSheet {
        lines = List.copyOf(lines);
    }
}
