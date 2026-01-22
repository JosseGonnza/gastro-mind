package com.gastromind.domain.service;

import com.gastromind.domain.entity.Batch;
import com.gastromind.domain.entity.Product;
import com.gastromind.domain.valueobject.Category;
import com.gastromind.domain.valueobject.Money;
import com.gastromind.domain.valueobject.Quantity;
import com.gastromind.domain.valueobject.UnitOfMeasure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CostingService debería")
class CostingServiceTest {

    private CostingService costingService;
    private Product rice;
    private Product chicken;
    private Product tomato;

    @BeforeEach
    void setUp() {
        costingService = new CostingService();

        rice = Product.create(
                "Arroz Bomba",
                "Especial para paella",
                Category.GRAIN,
                UnitOfMeasure.KILOGRAM,
                Set.of()
        );

        chicken = Product.create(
                "Pollo",
                "Muslos de pollo",
                Category.MEAT,
                UnitOfMeasure.KILOGRAM,
                Set.of()
        );

        tomato = Product.create(
                "Tomate",
                "Tomate triturado",
                Category.VEGETABLE,
                UnitOfMeasure.KILOGRAM,
                Set.of()
        );
    }

    @Nested
    @DisplayName("al calcular el coste de un ingrediente")
    class CalculateIngredientCost {

        @Test
        @DisplayName("calcular el coste con un lote")
        void shouldCalculateCostWithSingleBatch() {
            Batch batch = Batch.create(rice, "LOT-2026-001", LocalDate.now().plusMonths(6), Money.of(20.0), Quantity.of(10.0));
            List<Batch> batches = new ArrayList<>(List.of(batch));

            Money cost = costingService.calculateIngredientCost(rice, Quantity.of(3.0), batches);

            assertThat(cost.amount()).isEqualByComparingTo(new BigDecimal("6.00"));
            assertThat(cost.currency().getCurrencyCode()).isEqualTo("EUR");
        }
    }
}
