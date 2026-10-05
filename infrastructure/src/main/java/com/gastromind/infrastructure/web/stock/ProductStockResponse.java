package com.gastromind.infrastructure.web.stock;

import com.gastromind.application.stock.ProductStock;
import com.gastromind.domain.entity.Batch;
import com.gastromind.domain.entity.Product;
import com.gastromind.domain.valueobject.Allergen;
import com.gastromind.domain.valueobject.Category;
import com.gastromind.domain.valueobject.UnitOfMeasure;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProductStockResponse(UUID productId, String name, Category category, UnitOfMeasure unit,
                                   List<Allergen> allergens, BigDecimal total, List<BatchLine> batches) {

    public record BatchLine(UUID batchId, String lotCode, BigDecimal quantity, LocalDate entryDate,
                            LocalDate expirationDate) {

        static BatchLine from(Batch batch) {
            return new BatchLine(batch.getId(), batch.getSku(), batch.getCurrentQuantity().amount(),
                    batch.getEntryDate(), batch.getExpirationDate());
        }
    }

    public static ProductStockResponse from(ProductStock stock) {
        Product product = stock.product();
        return new ProductStockResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getUnit(),
                product.getAllergens().stream().sorted().toList(),
                stock.total().amount(),
                stock.batches().stream().map(BatchLine::from).toList()
        );
    }
}
