package com.gastromind.application.stock;

import com.gastromind.domain.entity.Batch;
import com.gastromind.domain.entity.Product;
import com.gastromind.domain.valueobject.Quantity;

import java.util.List;

public record ProductStock(Product product, Quantity total, List<Batch> batches) {
}
