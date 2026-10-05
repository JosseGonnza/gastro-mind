package com.gastromind.domain.entity;

import com.gastromind.domain.exception.DomainValidationException;
import com.gastromind.domain.valueobject.Allergen;
import com.gastromind.domain.valueobject.Category;
import com.gastromind.domain.valueobject.Money;
import com.gastromind.domain.valueobject.NewReceiptLine;
import com.gastromind.domain.valueobject.Quantity;
import com.gastromind.domain.valueobject.ReceiptLine;
import com.gastromind.domain.valueobject.ReceivedGoods;
import com.gastromind.domain.valueobject.UnitOfMeasure;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GoodsReceipt debería")
class GoodsReceiptTest {

    private static final Supplier SUPPLIER = Supplier.create("Pescados Cimadevilla", null, null, null);
    private static final Product PRAWNS = Product.create("Gamba roja", "De Huelva", Category.SEAFOOD,
            UnitOfMeasure.KILOGRAM, Set.of(Allergen.CRUSTACEANS));
    private static final Product HAKE = Product.create("Merluza de pincho", "Del Cantábrico", Category.FISH,
            UnitOfMeasure.KILOGRAM, Set.of(Allergen.FISH));
    private static final LocalDate IN_FOUR_DAYS = LocalDate.now().plusDays(4);
    private static final LocalDate IN_TWO_DAYS = LocalDate.now().plusDays(2);

    private static NewReceiptLine prawnsLine(String lotCode) {
        return new NewReceiptLine(PRAWNS, Quantity.of(2, UnitOfMeasure.KILOGRAM), Money.of(60.0), IN_FOUR_DAYS, lotCode);
    }

    private static NewReceiptLine hakeLine(String lotCode) {
        return new NewReceiptLine(HAKE, Quantity.of(3000, UnitOfMeasure.GRAM), Money.of(45.0), IN_TWO_DAYS, lotCode);
    }

    @Test
    @DisplayName("Registrar un albarán creando un lote por cada línea")
    void shouldRegisterReceiptCreatingOneBatchPerLine() {
        ReceivedGoods received = GoodsReceipt.register(SUPPLIER, "ALB-1234", List.of(prawnsLine("GR-778"), hakeLine("ME-12")));

        GoodsReceipt receipt = received.receipt();
        assertThat(receipt.getId()).isNotNull();
        assertThat(receipt.getSupplierId()).isEqualTo(SUPPLIER.getId());
        assertThat(receipt.getDeliveryNoteNumber()).isEqualTo("ALB-1234");
        assertThat(receipt.getReceivedOn()).isToday();

        List<Batch> batches = received.batches();
        assertThat(batches).hasSize(2);
        assertThat(batches).extracting(Batch::getProductId).containsExactly(PRAWNS.getId(), HAKE.getId());
        assertThat(batches).extracting(Batch::getSku).containsExactly("GR-778", "ME-12");
        assertThat(batches.get(1).getCurrentQuantity()).isEqualTo(Quantity.of(3, UnitOfMeasure.KILOGRAM));

        assertThat(receipt.getLines()).extracting(ReceiptLine::batchId)
                .containsExactly(batches.get(0).getId(), batches.get(1).getId());
    }

    @Test
    @DisplayName("Guardar en cada línea lo que entró: producto, cantidad, importe, caducidad y lote")
    void shouldKeepWhatArrivedInEachLine() {
        ReceivedGoods received = GoodsReceipt.register(SUPPLIER, "ALB-1234", List.of(hakeLine("ME-12")));

        ReceiptLine line = received.receipt().getLines().getFirst();
        assertThat(line.productId()).isEqualTo(HAKE.getId());
        assertThat(line.quantity()).isEqualTo(Quantity.of(3, UnitOfMeasure.KILOGRAM));
        assertThat(line.amount().amount()).isEqualByComparingTo(new BigDecimal("45.00"));
        assertThat(line.expirationDate()).isEqualTo(IN_TWO_DAYS);
        assertThat(line.lotCode()).isEqualTo("ME-12");
    }

    @Test
    @DisplayName("Usar el número de albarán y la línea como lote si el proveedor no da uno")
    void shouldUseDeliveryNoteNumberAsLotCodeWhenMissing() {
        ReceivedGoods received = GoodsReceipt.register(SUPPLIER, " ALB-1234 ", List.of(prawnsLine("GR-778"), hakeLine(" ")));

        assertThat(received.receipt().getDeliveryNoteNumber()).isEqualTo("ALB-1234");
        assertThat(received.batches().get(1).getSku()).isEqualTo("ALB-1234-2");
    }

    @Test
    @DisplayName("Calcular el total del albarán")
    void shouldCalculateTotal() {
        ReceivedGoods received = GoodsReceipt.register(SUPPLIER, "ALB-1234", List.of(prawnsLine("GR-778"), hakeLine("ME-12")));

        assertThat(received.receipt().total().amount()).isEqualByComparingTo(new BigDecimal("105.00"));
    }

    @Test
    @DisplayName("No registrar un albarán sin proveedor, sin número o sin líneas")
    void shouldNotRegisterIncompleteReceipt() {
        assertThatThrownBy(() -> GoodsReceipt.register(null, "ALB-1234", List.of(prawnsLine(null))))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Supplier cannot be null");
        assertThatThrownBy(() -> GoodsReceipt.register(SUPPLIER, " ", List.of(prawnsLine(null))))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Delivery note number cannot be empty");
        assertThatThrownBy(() -> GoodsReceipt.register(SUPPLIER, "ALB-1234", List.of()))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("A goods receipt needs at least one line");
    }

    @Test
    @DisplayName("No registrar nada si alguna línea no es válida")
    void shouldNotRegisterWhenALineIsInvalid() {
        NewReceiptLine expired = new NewReceiptLine(HAKE, Quantity.of(1, UnitOfMeasure.KILOGRAM), Money.of(15.0),
                LocalDate.now().minusDays(1), "ME-11");

        assertThatThrownBy(() -> GoodsReceipt.register(SUPPLIER, "ALB-1234", List.of(prawnsLine("GR-778"), expired)))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Cannot accept expired products");
    }

    @Test
    @DisplayName("Reconstruir un albarán guardado a partir de sus lotes")
    void shouldRestoreReceiptFromItsBatches() {
        ReceivedGoods received = GoodsReceipt.register(SUPPLIER, "ALB-1234", List.of(prawnsLine("GR-778"), hakeLine("ME-12")));
        GoodsReceipt original = received.receipt();
        UUID id = original.getId();
        LocalDate lastWeek = LocalDate.now().minusDays(7);

        GoodsReceipt restored = GoodsReceipt.restore(id, SUPPLIER.getId(), "ALB-1234", lastWeek, received.batches());

        assertThat(restored.getId()).isEqualTo(id);
        assertThat(restored.getReceivedOn()).isEqualTo(lastWeek);
        assertThat(restored.getLines()).isEqualTo(original.getLines());
    }
}
