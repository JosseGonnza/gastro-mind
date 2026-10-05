package com.gastromind.application.receipt;

import com.gastromind.domain.entity.GoodsReceipt;
import com.gastromind.domain.entity.Product;
import com.gastromind.domain.entity.Supplier;
import com.gastromind.domain.valueobject.Category;
import com.gastromind.domain.valueobject.Money;
import com.gastromind.domain.valueobject.NewReceiptLine;
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

@DisplayName("GetGoodsReceipt debería")
class GetGoodsReceiptTest {

    private final InMemoryGoodsReceiptRepository goodsReceiptRepository = new InMemoryGoodsReceiptRepository();
    private final GetGoodsReceipt getGoodsReceipt = new GetGoodsReceipt(goodsReceiptRepository);

    @Test
    @DisplayName("devolver el albarán si existe")
    void shouldReturnReceiptWhenItExists() {
        Supplier supplier = Supplier.create("Frutas Pepe", null, null, null);
        Product tomato = Product.create("Tomate pera", null, Category.VEGETABLE, UnitOfMeasure.KILOGRAM, Set.of());
        ReceivedGoods received = GoodsReceipt.register(supplier, "F-88", List.of(
                new NewReceiptLine(tomato, Quantity.of(10, UnitOfMeasure.KILOGRAM), Money.of(18.0), LocalDate.now().plusDays(6), null)));
        goodsReceiptRepository.save(received);

        assertThat(getGoodsReceipt.execute(received.receipt().getId())).isSameAs(received.receipt());
    }

    @Test
    @DisplayName("lanzar GoodsReceiptNotFoundException si no existe")
    void shouldThrowExceptionWhenReceiptDoesNotExist() {
        UUID unknownId = UUID.randomUUID();

        assertThatThrownBy(() -> getGoodsReceipt.execute(unknownId))
                .isInstanceOf(GoodsReceiptNotFoundException.class)
                .hasMessage("Goods receipt not found: " + unknownId);
    }
}
