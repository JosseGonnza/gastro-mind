package com.gastromind.domain.entity;

import com.gastromind.domain.valueobject.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Batch debería")
public class BatchTest {

    private static final Product PRODUCT = Product.create(
            "Harina",
            "Harina de Trigo",
            Category.GRAIN,
            UnitOfMeasure.KILOGRAM,
            Set.of(Allergen.GLUTEN));
    private static final String SKU = "LOT-2026-001";
    private static final LocalDate EXPIRATION_DATE = LocalDate.now().plusMonths(6);
    private static final Money PURCHASE_PRICE = Money.of(50.00);
    private static final Quantity INITIAL_QUANTITY = Quantity.of(25.0);

    @Test
    @DisplayName("Crear un lote válido con stock inicial igual que el actual")
    void shouldCreateValidBatch() {
        Batch batch = Batch.create(PRODUCT, SKU, EXPIRATION_DATE, PURCHASE_PRICE, INITIAL_QUANTITY);

        assertThat(batch.getId()).isNotNull();
        assertThat(batch.getSku()).isEqualTo(SKU);
        assertThat(batch.getCurrentQuantity().value()).isEqualTo(25.0);
        assertThat(batch.getEntryDate()).isToday();
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


}
