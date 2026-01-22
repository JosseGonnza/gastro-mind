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

import static org.assertj.core.api.Assertions.*;

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
        @DisplayName("aplicar el método FEFO (consume los lotes que caducan antes, primero)")
        void shouldApplyFefoConsumingBatchesExpireFirst() {
            LocalDate soonExpiry = LocalDate.now().plusMonths(1);
            LocalDate laterExpiry = LocalDate.now().plusMonths(6);
            Batch expiringBatch = Batch.create(product, "LOT-EXPIRING", soonExpiry, Money.of(50.0), Quantity.of(5.0));
            Batch freshBatch = Batch.create(product, "LOT-FRESH", laterExpiry, Money.of(50.0), Quantity.of(10.0));
            List<Batch> batches = new ArrayList<>(List.of(expiringBatch, freshBatch));

            inventoryService.consumeProduct(product, Quantity.of(8), batches);

            assertThat(expiringBatch.getCurrentQuantity().value()).isEqualTo(0.0);
            assertThat(freshBatch.getCurrentQuantity().value()).isEqualTo(7.0);
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

        @Test
        @DisplayName("no modificar ningún lote si no hay stock suficiente")
            //Comprobamos la atomicidad de la operación
        void shouldNotModifyAnyBatchWhenNotEnoughStock() {
            Batch batch1 = Batch.create(product, "LOT-2026-001", LocalDate.now().plusMonths(6), Money.of(50.0), Quantity.of(5.0));
            Batch batch2 = Batch.create(product, "LOT-2026-002", LocalDate.now().plusMonths(5), Money.of(50.0), Quantity.of(3.0));
            List<Batch> batches = new ArrayList<>(List.of(batch1, batch2));

            try {
                inventoryService.consumeProduct(product, Quantity.of(10.0), batches);
            } catch (NotEnoughStockException e) {
                //Solo capturamos la excepción, pero no la lanzamos para poder llegar a los asserts
            }

            assertThat(batch1.getCurrentQuantity().value()).isEqualTo(5.0);
            assertThat(batch2.getCurrentQuantity().value()).isEqualTo(3.0);
        }

        @Test
        @DisplayName("ordenar por fecha de entrada")
        void shouldSortByEntryDate() {
            LocalDate expiry1 = LocalDate.now().plusMonths(1);
            LocalDate expiry2 = LocalDate.now().plusMonths(3);
            LocalDate expiry3 = LocalDate.now().plusMonths(6);
            Batch batch2 = Batch.create(product, "LOT-2026-002", expiry2, Money.of(45.0), Quantity.of(5.0));
            Batch batch3 = Batch.create(product, "LOT-2026-003", expiry3, Money.of(40.0), Quantity.of(5.0));
            Batch batch1 = Batch.create(product, "LOT-2026-001", expiry1, Money.of(50.0), Quantity.of(5.0));
            List<Batch> batches = new ArrayList<>(List.of(batch2, batch3, batch1));

            inventoryService.consumeProduct(product, Quantity.of(7.0), batches);

            assertThat(batch1.getCurrentQuantity().value()).isEqualTo(0.0);  // Gastamos el viejo
            assertThat(batch2.getCurrentQuantity().value()).isEqualTo(3.0);  // Consumimos una parte
            assertThat(batch3.getCurrentQuantity().value()).isEqualTo(5.0);  // No lo tocamos
        }

        @Test
        @DisplayName("lanzar excepción si el producto es null")
        void shouldThrowExceptionWhenProductIsNull() {
            List<Batch> batches = List.of();

            assertThatThrownBy(() -> inventoryService.consumeProduct(null, Quantity.of(5.0), batches))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Product cannot be null");
        }

        @Test
        @DisplayName("lanzar excepción si la cantidad a consumir es null")
        void shouldThrowExceptionWhenAmountIsNull() {
            List<Batch> batches = List.of();

            assertThatThrownBy(() -> inventoryService.consumeProduct(product, null, batches))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Amount to consume cannot be null");
        }

        @Test
        @DisplayName("lanzar excepción si la cantidad a consumir es cero o negativa")
        void shouldThrowExceptionWhenAmountIsZeroOrNegative() {
            List<Batch> batches = List.of();

            assertThatThrownBy(() -> inventoryService.consumeProduct(product, Quantity.of(0.0), batches))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Amount to consume must be greater than zero");
        }

        @Test
        @DisplayName("lanzar excepción si la lista de lotes es null")
        void shouldThrowExceptionWhenBatchesIsNull() {
            assertThatThrownBy(() -> inventoryService.consumeProduct(product, Quantity.of(5.0), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Batches list cannot be null");
        }
    }
}
