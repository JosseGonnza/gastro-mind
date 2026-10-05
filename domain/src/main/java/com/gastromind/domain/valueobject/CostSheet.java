package com.gastromind.domain.valueobject;

import java.util.List;

//El escandallo de una receta
public record CostSheet(List<CostSheetLine> lines, Money totalCost, Money costPerPortion) {

    public CostSheet {
        lines = List.copyOf(lines);
    }
}
