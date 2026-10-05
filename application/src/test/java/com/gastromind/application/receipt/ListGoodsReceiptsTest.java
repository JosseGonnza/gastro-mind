package com.gastromind.application.receipt;

import com.gastromind.domain.entity.Batch;
import com.gastromind.domain.entity.GoodsReceipt;
import com.gastromind.domain.entity.Product;
import com.gastromind.domain.exception.DomainValidationException;
import com.gastromind.domain.valueobject.Category;
import com.gastromind.domain.valueobject.Money;
import com.gastromind.domain.valueobject.Quantity;
import com.gastromind.domain.valueobject.ReceivedGoods;
import com.gastromind.domain.valueobject.UnitOfMeasure;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ListGoodsReceipts debería")
class ListGoodsReceiptsTest {

    private final InMemoryGoodsReceiptRepository goodsReceiptRepository = new InMemoryGoodsReceiptRepository();
    private final ListGoodsReceipts listGoodsReceipts = new ListGoodsReceipts(goodsReceiptRepository);
    private final Product tomato = Product.create("Tomate pera", null, Category.VEGETABLE, UnitOfMeasure.KILOGRAM, Set.of());

    private void saveReceipt(UUID supplierId, String number, LocalDate receivedOn) {
        Batch batch = Batch.restore(UUID.randomUUID(), tomato.getId(), UnitOfMeasure.KILOGRAM, number + "-1", receivedOn,
                LocalDate.now().plusDays(10), Money.of(18.0), Quantity.of(10, UnitOfMeasure.KILOGRAM),
                Quantity.of(10, UnitOfMeasure.KILOGRAM));
        GoodsReceipt receipt = GoodsReceipt.restore(UUID.randomUUID(), supplierId, number, receivedOn, List.of(batch));
        goodsReceiptRepository.save(new ReceivedGoods(receipt, List.of(batch)));
    }

    @Test
    @DisplayName("listar los albaranes del filtro, primero los más recientes")
    void shouldListReceiptsNewestFirst() {
        UUID supplier = UUID.randomUUID();
        saveReceipt(supplier, "F-87", LocalDate.now().minusDays(7));
        saveReceipt(supplier, "F-88", LocalDate.now());
        ReceiptFilter filter = new ReceiptFilter(supplier, null, null);

        List<GoodsReceiptSummary> summaries = listGoodsReceipts.execute(filter);

        assertThat(summaries).extracting(GoodsReceiptSummary::deliveryNoteNumber).containsExactly("F-88", "F-87");
        assertThat(goodsReceiptRepository.lastFilter()).isEqualTo(filter);
    }

    @Test
    @DisplayName("rechazar un rango de fechas al revés")
    void shouldRejectInvertedDateRange() {
        ReceiptFilter inverted = new ReceiptFilter(null, LocalDate.now(), LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> listGoodsReceipts.execute(inverted))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("From date cannot be after to date");
    }

    @Test
    @DisplayName("listar todos si no se pasa filtro")
    void shouldListEverythingWithoutFilter() {
        saveReceipt(UUID.randomUUID(), "F-88", LocalDate.now());

        assertThat(listGoodsReceipts.execute(null)).hasSize(1);
        assertThat(goodsReceiptRepository.lastFilter()).isEqualTo(ReceiptFilter.all());
    }
}
