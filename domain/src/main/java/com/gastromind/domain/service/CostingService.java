package com.gastromind.domain.service;

import com.gastromind.domain.entity.Batch;
import com.gastromind.domain.entity.Product;
import com.gastromind.domain.valueobject.Money;
import com.gastromind.domain.valueobject.Quantity;

import java.math.BigDecimal;
import java.util.List;

public class CostingService {
    public Money calculateIngredientCost(Product product, Quantity quantity, List<Batch> batches) {
        BigDecimal costOfBatch = batches.getFirst().getUnitCost().amount();
        return Money.of(costOfBatch.multiply(BigDecimal.valueOf(quantity.value())));
    }
}
