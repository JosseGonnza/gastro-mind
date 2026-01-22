package com.gastromind.domain.service;

import com.gastromind.domain.entity.Batch;
import com.gastromind.domain.entity.Product;
import com.gastromind.domain.exception.NotEnoughStockException;
import com.gastromind.domain.valueobject.Category;
import com.gastromind.domain.valueobject.Money;
import com.gastromind.domain.valueobject.Quantity;
import com.gastromind.domain.valueobject.UnitOfMeasure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("InventoryService debería")
class InventoryServiceTest {

    public InventoryService inventoryService;
    private Product product;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService();

        product = Product.create(
                "Arroz Bomba",
                "Especial para paella",
                Category.GRAIN,
                UnitOfMeasure.GRAM,
                Set.of()
        );
    }

    @Nested
    @DisplayName("al calcular stock disponible")
    class CalculateCurrentStock {

        @Test
        @DisplayName("sumar las cantidades actuales de todos los lotes")
        void shouldSumCurrentQuantitiesOfBatches() {
            List<Batch> bathes = List.of(
                    Batch.create(product, "LOT-2026-001", LocalDate.now().plusMonths(6), Money.of(50.0), Quantity.of(10.0)),
                    Batch.create(product, "LOT-2026-002", LocalDate.now().plusMonths(5), Money.of(50.0), Quantity.of(5.0)),
                    Batch.create(product, "LOT-2026-003", LocalDate.now().plusMonths(4), Money.of(50.0), Quantity.of(2.0))
            );

            Quantity totalStock = inventoryService.calculateCurrentStock(product, bathes);

            assertThat(totalStock.value()).isEqualTo(17.0);
        }

        @Test
        @DisplayName("fallar con un producto nulo")
        void shouldThrowExceptionWhenProductIsNull() {
            List<Batch> batches = List.of(
                    Batch.create(product, "LOT-2026-001", LocalDate.now().plusMonths(6), Money.of(50.0), Quantity.of(10.0)),
                    Batch.create(product, "LOT-2026-002", LocalDate.now().plusMonths(5), Money.of(50.0), Quantity.of(5.0)),
                    Batch.create(product, "LOT-2026-003", LocalDate.now().plusMonths(4), Money.of(50.0), Quantity.of(2.0))
            );

            assertThatThrownBy(() -> inventoryService.calculateCurrentStock(null, batches))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Product cannot be null");
        }

        @Test
        @DisplayName("devolver cero con una lista vacía")
        void shouldReturnZeroWhenListIsEmpty() {
            List<Batch> emptyList = List.of();

            Quantity totalStock = inventoryService.calculateCurrentStock(product, emptyList);

            assertThat(totalStock.value()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("devolver cero si la lista de lotes es null")
        void shouldReturnZeroWhenListIsNull() {
            Quantity totalStock = inventoryService.calculateCurrentStock(product, null);

            assertThat(totalStock.value()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("al consumir productos")
    class ConsumeProduct {

        @Test
        @DisplayName("consumir de un único lote si hay suficiente stock")
        void shouldConsumeFromSingleBatchWhenEnoughStock() {
            Batch batch = Batch.create(product, "LOT-2026-001", LocalDate.now().plusMonths(6), Money.of(50.0), Quantity.of(10.0));
            List<Batch> batches = new ArrayList<>(List.of(batch));

            inventoryService.consumeProduct(product, Quantity.of(3.0), batches);

            assertThat(batch.getCurrentQuantity().value()).isEqualTo(7.0);
        }

        @Test
        @DisplayName("aplicar el método FIFO (consume los lotes más antiguos primero)")
        void shouldApplyFifoConsumingOldestBatchesFirst() {
            LocalDate oldDate = LocalDate.now().minusDays(10);
            LocalDate recentDate = LocalDate.now().minusDays(5);
            Batch oldBatch = Batch.create(product, "LOT-OLD", oldDate.plusMonths(6), Money.of(50.0), Quantity.of(5.0));
            Batch recentBatch = Batch.create(product, "LOT-RECENT", recentDate.plusMonths(6), Money.of(50.0), Quantity.of(10.0));
            List<Batch> batches = new ArrayList<>(List.of(oldBatch, recentBatch));

            inventoryService.consumeProduct(product, Quantity.of(8), batches);

            assertThat(oldBatch.getCurrentQuantity().value()).isEqualTo(0.0);
            assertThat(recentBatch.getCurrentQuantity().value()).isEqualTo(7.0);
        }

        @Test
        @DisplayName("consumir todo el stock disponible")
        void shouldConsumeAllAvailableStock() {
            Batch batch1 = Batch.create(product, "LOT-2026-001", LocalDate.now().plusMonths(6), Money.of(50.0), Quantity.of(5.0));
            Batch batch2 = Batch.create(product, "LOT-2026-002", LocalDate.now().plusMonths(5), Money.of(50.0), Quantity.of(10.0));
            List<Batch> batches = new ArrayList<>(List.of(batch1, batch2));

            inventoryService.consumeProduct(product, Quantity.of(15.0), batches);

            assertThat(batch1.getCurrentQuantity().value()).isEqualTo(0.0);
            assertThat(batch2.getCurrentQuantity().value()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("fallar cuando no hay suficiente stock")
        void shouldThrowExceptionWhenInsufficientStock() {
            Batch batch = Batch.create(product, "LOT-2026-001", LocalDate.now().plusMonths(6), Money.of(50.0), Quantity.of(5.0));
            List<Batch> batches = new ArrayList<>(List.of(batch));

            assertThatThrownBy(() -> inventoryService.consumeProduct(product, Quantity.of(10.0), batches))
                    .isInstanceOf(NotEnoughStockException.class)
                    .hasMessageContaining("Not enough stock for product Arroz Bomba")
                    .hasMessageContaining("Requested: 10.0")
                    .hasMessageContaining("Available: 5.0");
        }
    }
}
