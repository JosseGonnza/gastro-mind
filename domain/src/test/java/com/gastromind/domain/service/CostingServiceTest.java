package com.gastromind.domain.service;

import com.gastromind.domain.entity.Batch;
import com.gastromind.domain.entity.Product;
import com.gastromind.domain.entity.Recipe;
import com.gastromind.domain.exception.NotEnoughStockException;
import com.gastromind.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        @Test
        @DisplayName("calcular el coste con múltiples lotes")
        void shouldCalculateCostWithSeveralBatches() {
            Batch batch1 = Batch.create(rice, "LOT-2026-001", LocalDate.now().plusMonths(5),
                    Money.of(20.0), Quantity.of(2.0));
            Batch batch2 = Batch.create(rice, "LOT-2026-002", LocalDate.now().plusMonths(6),
                    Money.of(10.0), Quantity.of(5.0));
            List<Batch> batches = new ArrayList<>(List.of(batch1, batch2));

            Money cost = costingService.calculateIngredientCost(rice, Quantity.of(4.0), batches);

            assertThat(cost.amount()).isEqualByComparingTo(new BigDecimal("24.00"));
        }

        @Test
        @DisplayName("calcular el coste exacto agotando los lotes")
        void shouldCalculateCostExhaustingAllBatches() {
            Batch batch1 = Batch.create(rice, "LOT-2026-001", LocalDate.now().plusMonths(5),
                    Money.of(6.0), Quantity.of(2.0));
            Batch batch2 = Batch.create(rice, "LOT-2026-002", LocalDate.now().plusMonths(6),
                    Money.of(6.0), Quantity.of(3.0));
            List<Batch> batches = new ArrayList<>(List.of(batch1, batch2));

            Money cost = costingService.calculateIngredientCost(rice, Quantity.of(5.0), batches);

            assertThat(cost.amount()).isEqualByComparingTo(new BigDecimal("12.00"));
        }

        @Test
        @DisplayName("lanzar excepción si no hay suficiente stock")
        void shouldThrowExceptionWhenInsufficientStock() {
            Batch batch = Batch.create(rice, "LOT-2026-002", LocalDate.now().plusMonths(6),
                    Money.of(6.0), Quantity.of(3.0));
            List<Batch> batches = new ArrayList<>(List.of(batch));

            assertThatThrownBy(() -> costingService.calculateIngredientCost(rice, Quantity.of(5.0), batches))
                    .isInstanceOf(NotEnoughStockException.class)
                    .hasMessageContaining("Not enough stock for product Arroz Bomba")
                    .hasMessageContaining("Requested: 5.0")
                    .hasMessageContaining("Available: 3.0");
        }

        @Test
        @DisplayName("calcular el coste con decimales precisos")
        void shouldCalculateCostWithPreciseDecimals() {
            Batch batch = Batch.create(rice, "LOT-2026-002", LocalDate.now().plusMonths(6),
                    Money.of(25.0), Quantity.of(10.0));
            List<Batch> batches = new ArrayList<>(List.of(batch));

            Money cost = costingService.calculateIngredientCost(rice, Quantity.of(3.7), batches);

            assertThat(cost.amount()).isEqualByComparingTo(new BigDecimal("9.25"));
        }

        @Test
        @DisplayName("lanzar excepción si el producto es null")
        void shouldThrowExceptionWhenProductIsNull() {
            List<Batch> batches = List.of();

            assertThatThrownBy(() -> costingService.calculateIngredientCost(null, Quantity.of(5.0), batches))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Product cannot be null");
        }

        @Test
        @DisplayName("lanzar excepción si la cantidad es null")
        void shouldThrowExceptionWhenQuantityIsNull() {
            List<Batch> batches = List.of();

            assertThatThrownBy(() -> costingService.calculateIngredientCost(rice, null, batches))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Quantity cannot be null");
        }

        @Test
        @DisplayName("lanzar excepción si la cantidad es cero o negativa")
        void shouldThrowExceptionWhenQuantityIsZeroOrNegative() {
            List<Batch> batches = List.of();

            assertThatThrownBy(() -> costingService.calculateIngredientCost(rice, Quantity.of(0.0), batches))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Quantity must be greater than zero");
        }

        @Test
        @DisplayName("lanzar excepción si la lista de lotes es null")
        void shouldThrowExceptionWhenBatchesIsNull() {
            assertThatThrownBy(() -> costingService.calculateIngredientCost(rice, Quantity.of(5.0), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Batches list cannot be null");
        }
    }

    @Nested
    @DisplayName("al calcular el coste de una receta")
    class CalculateRecipeCost {

        @Test
        @DisplayName("calcular el coste con precios estables")
        void shouldCalculateRecipeCostWithStablePrices() {
            Recipe recipe = Recipe.create(
                    "Arroz con pollo",
                    "Receta sencilla",
                    Duration.ofMinutes(30),
                    Difficulty.EASY,
                    4
            );
            recipe.addIngredient(RecipeIngredient.of(rice, Quantity.of(1)));
            recipe.addIngredient(RecipeIngredient.of(chicken, Quantity.of(0.5)));
            Map<Product, List<Batch>> availableBatches = new HashMap<>();
            availableBatches.put(rice, List.of(
                    Batch.create(rice, "LOT-2026-001", LocalDate.now().plusMonths(6), Money.of(20.0), Quantity.of(10.0))
            ));
            availableBatches.put(chicken, List.of(
                    Batch.create(rice, "LOT-2026-002", LocalDate.now().plusMonths(1), Money.of(40.0), Quantity.of(5.0))
            ));

            Money totalCost = costingService.calculateRecipeCost(recipe, availableBatches);

            assertThat(totalCost.amount()).isEqualByComparingTo(new BigDecimal("6.00"));
        }
    }
}
