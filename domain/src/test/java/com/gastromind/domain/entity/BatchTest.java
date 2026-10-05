package com.gastromind.domain.entity;

import com.gastromind.domain.exception.DomainValidationException;
import com.gastromind.domain.valueobject.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Batch debería")
class BatchTest {

    private static final Product PRODUCT = Product.create(
            "Harina",
            "Harina de Trigo",
            Category.GRAIN,
            UnitOfMeasure.KILOGRAM,
            Set.of(Allergen.GLUTEN));
    private static final String SKU = "LOT-2026-001";
    private static final LocalDate EXPIRATION_DATE = LocalDate.now().plusMonths(6);
    private static final Money PURCHASE_PRICE = Money.of(50.00);
    private static final Quantity INITIAL_QUANTITY = Quantity.of(25.0, UnitOfMeasure.KILOGRAM);

    @Test
    @DisplayName("Crear un lote válido con stock inicial igual que el actual")
    void shouldCreateValidBatch() {
        Batch batch = Batch.create(PRODUCT, SKU, EXPIRATION_DATE, PURCHASE_PRICE, INITIAL_QUANTITY);

        assertThat(batch.getId()).isNotNull();
        assertThat(batch.getSku()).isEqualTo(SKU);
        assertThat(batch.getCurrentQuantity()).isEqualTo(Quantity.of(25.0, UnitOfMeasure.KILOGRAM));
        assertThat(batch.getEntryDate()).isToday();
    }

    @Test
    @DisplayName("Guardar del producto solo su id y su unidad")
    void shouldKeepProductIdAndUnit() {
        Batch batch = Batch.create(PRODUCT, SKU, EXPIRATION_DATE, PURCHASE_PRICE, INITIAL_QUANTITY);

        assertThat(batch.getProductId()).isEqualTo(PRODUCT.getId());
        assertThat(batch.getUnit()).isEqualTo(UnitOfMeasure.KILOGRAM);
    }

    @Test
    @DisplayName("Calcular el coste unitario Precio/Cantidad")
    void shouldCalculateUnitCost() {
        //50€ / 25kg = 2€/Kg
        Batch batch = Batch.create(PRODUCT, SKU, EXPIRATION_DATE, PURCHASE_PRICE, INITIAL_QUANTITY);

        Money unitCost = batch.getUnitCost();

        //isEqualByComparingTo no diferencia entre 2.0 y 2.00 (Mejor que isEqualTo)
        assertThat(unitCost.amount()).isEqualByComparingTo(new BigDecimal("2.00"));
        assertThat(unitCost.currency()).isEqualTo(PURCHASE_PRICE.currency());
    }

    @Test
    @DisplayName("Reducir el stock al consumir")
    void shouldReduceStockWhenConsuming() {
        Batch batch = Batch.create(PRODUCT, SKU, EXPIRATION_DATE, PURCHASE_PRICE, INITIAL_QUANTITY);

        batch.consume(Quantity.of(5.0, UnitOfMeasure.KILOGRAM));

        assertThat(batch.getCurrentQuantity()).isEqualTo(Quantity.of(20.0, UnitOfMeasure.KILOGRAM));
    }

    @Test
    @DisplayName("Lanzar error si intentamos consumir más de lo que hay")
    void shouldThrowExceptionWhenOverConsuming() {
        Batch batch = Batch.create(PRODUCT, SKU, EXPIRATION_DATE, PURCHASE_PRICE, INITIAL_QUANTITY);

        assertThatThrownBy(() -> batch.consume(Quantity.of(30.0, UnitOfMeasure.KILOGRAM)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not enough quantity available");
    }

    @Test
    @DisplayName("Guardar la cantidad en la unidad del producto")
    void shouldStoreQuantityInProductUnit() {
        //5000 g de harina, que se gestiona en kg: 50€ / 5kg = 10€/Kg
        Batch batch = Batch.create(PRODUCT, SKU, EXPIRATION_DATE, PURCHASE_PRICE, Quantity.of(5000, UnitOfMeasure.GRAM));

        assertThat(batch.getInitialQuantity()).isEqualTo(Quantity.of(5, UnitOfMeasure.KILOGRAM));
        assertThat(batch.getCurrentQuantity()).isEqualTo(Quantity.of(5, UnitOfMeasure.KILOGRAM));
        assertThat(batch.getUnitCost().amount()).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @Test
    @DisplayName("No aceptar una cantidad en una unidad que no casa con la del producto")
    void shouldThrowExceptionWhenQuantityUnitIsIncompatible() {
        assertThatThrownBy(() -> Batch.create(PRODUCT, SKU, EXPIRATION_DATE, PURCHASE_PRICE, Quantity.of(3, UnitOfMeasure.UNIT)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity unit must be compatible with product unit");
    }

    @Test
    @DisplayName("No aceptar un lote sin cantidad")
    void shouldThrowExceptionWhenInitialQuantityIsZero() {
        assertThatThrownBy(() -> Batch.create(PRODUCT, SKU, EXPIRATION_DATE, PURCHASE_PRICE, Quantity.of(0, UnitOfMeasure.KILOGRAM)))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Initial quantity must be greater than zero");
    }

    @Test
    @DisplayName("No permitir crear lotes ya caducados")
    void shouldThrowExceptionWhenIfExpired() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        assertThatThrownBy(() -> Batch.create(PRODUCT, SKU, yesterday, PURCHASE_PRICE, INITIAL_QUANTITY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot accept expired products");
    }

    @Test
    @DisplayName("Reconstruir un lote guardado tal como estaba, aunque ya haya caducado")
    void shouldRestoreStoredBatchEvenIfExpired() {
        UUID id = UUID.randomUUID();
        LocalDate entryDate = LocalDate.now().minusDays(30);
        LocalDate yesterday = LocalDate.now().minusDays(1);

        Batch batch = Batch.restore(id, PRODUCT.getId(), PRODUCT.getUnit(), SKU, entryDate, yesterday, PURCHASE_PRICE,
                INITIAL_QUANTITY, Quantity.of(10, UnitOfMeasure.KILOGRAM));

        assertThat(batch.getId()).isEqualTo(id);
        assertThat(batch.getEntryDate()).isEqualTo(entryDate);
        assertThat(batch.getExpirationDate()).isEqualTo(yesterday);
        assertThat(batch.getInitialQuantity()).isEqualTo(INITIAL_QUANTITY);
        assertThat(batch.getCurrentQuantity()).isEqualTo(Quantity.of(10, UnitOfMeasure.KILOGRAM));
    }

    @Test
    @DisplayName("Reconstruir la cantidad actual en la unidad del producto")
    void shouldRestoreCurrentQuantityInProductUnit() {
        Batch batch = Batch.restore(UUID.randomUUID(), PRODUCT.getId(), PRODUCT.getUnit(), SKU, LocalDate.now(), EXPIRATION_DATE, PURCHASE_PRICE,
                INITIAL_QUANTITY, Quantity.of(10000, UnitOfMeasure.GRAM));

        assertThat(batch.getCurrentQuantity()).isEqualTo(Quantity.of(10, UnitOfMeasure.KILOGRAM));
    }

    @Test
    @DisplayName("No reconstruir un lote con más cantidad actual que inicial")
    void shouldNotRestoreBatchWithMoreCurrentThanInitialQuantity() {
        assertThatThrownBy(() -> Batch.restore(UUID.randomUUID(), PRODUCT.getId(), PRODUCT.getUnit(), SKU, LocalDate.now(), EXPIRATION_DATE, PURCHASE_PRICE,
                INITIAL_QUANTITY, Quantity.of(30, UnitOfMeasure.KILOGRAM)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Current quantity cannot exceed initial quantity");
    }

    @Test
    @DisplayName("No reconstruir un lote sin cantidad actual, fecha de entrada o producto")
    void shouldNotRestoreBatchWithMissingData() {
        assertThatThrownBy(() -> Batch.restore(UUID.randomUUID(), PRODUCT.getId(), PRODUCT.getUnit(), SKU, LocalDate.now(), EXPIRATION_DATE, PURCHASE_PRICE,
                INITIAL_QUANTITY, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Current quantity cannot be null");
        assertThatThrownBy(() -> Batch.restore(UUID.randomUUID(), PRODUCT.getId(), PRODUCT.getUnit(), SKU, null, EXPIRATION_DATE, PURCHASE_PRICE,
                INITIAL_QUANTITY, INITIAL_QUANTITY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Entry date cannot be null");
        assertThatThrownBy(() -> Batch.restore(UUID.randomUUID(), null, UnitOfMeasure.KILOGRAM, SKU, LocalDate.now(), EXPIRATION_DATE,
                PURCHASE_PRICE, INITIAL_QUANTITY, INITIAL_QUANTITY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product cannot be null");
    }
}
