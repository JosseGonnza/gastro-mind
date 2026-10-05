package com.gastromind.application.receipt;

import com.gastromind.application.product.InMemoryProductRepository;
import com.gastromind.application.product.ProductNotFoundException;
import com.gastromind.application.supplier.InMemorySupplierRepository;
import com.gastromind.application.supplier.SupplierNotFoundException;
import com.gastromind.domain.entity.Batch;
import com.gastromind.domain.entity.GoodsReceipt;
import com.gastromind.domain.entity.Product;
import com.gastromind.domain.entity.Supplier;
import com.gastromind.domain.exception.DomainValidationException;
import com.gastromind.domain.valueobject.Category;
import com.gastromind.domain.valueobject.Quantity;
import com.gastromind.domain.valueobject.UnitOfMeasure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

@DisplayName("ReceiveGoods debería")
class ReceiveGoodsTest {

    private InMemoryGoodsReceiptRepository goodsReceiptRepository;
    private ReceiveGoods receiveGoods;
    private Supplier supplier;
    private Product prawns;
    private Product hake;

    @BeforeEach
    void setUp() {
        InMemorySupplierRepository supplierRepository = new InMemorySupplierRepository();
        InMemoryProductRepository productRepository = new InMemoryProductRepository();
        goodsReceiptRepository = new InMemoryGoodsReceiptRepository();
        receiveGoods = new ReceiveGoods(supplierRepository, productRepository, goodsReceiptRepository);

        supplier = Supplier.create("Pescados Cimadevilla", null, null, null);
        prawns = Product.create("Gamba roja", null, Category.SEAFOOD, UnitOfMeasure.KILOGRAM, Set.of());
        hake = Product.create("Merluza de pincho", null, Category.FISH, UnitOfMeasure.KILOGRAM, Set.of());
        supplierRepository.save(supplier);
        productRepository.save(prawns);
        productRepository.save(hake);
    }

    private ReceiveGoodsCommand.Line line(UUID productId, String lotCode) {
        return new ReceiveGoodsCommand.Line(productId, new BigDecimal("2500"), UnitOfMeasure.GRAM,
                new BigDecimal("37.50"), LocalDate.now().plusDays(3), lotCode);
    }

    @Test
    @DisplayName("registrar el albarán y guardarlo con sus lotes")
    void shouldRegisterAndSaveTheReceiptWithItsBatches() {
        ReceiveGoodsCommand command = new ReceiveGoodsCommand(supplier.getId(), "ALB-1234",
                List.of(line(prawns.getId(), "GR-778"), line(hake.getId(), null)));

        GoodsReceipt receipt = receiveGoods.execute(command);

        assertThat(goodsReceiptRepository.findById(receipt.getId())).containsSame(receipt);
        assertThat(goodsReceiptRepository.savedBatches())
                .extracting(Batch::getSku, Batch::getCurrentQuantity)
                .containsExactly(
                        tuple("GR-778", Quantity.of(2.5, UnitOfMeasure.KILOGRAM)),
                        tuple("ALB-1234-2", Quantity.of(2.5, UnitOfMeasure.KILOGRAM)));
        assertThat(receipt.total().amount()).isEqualByComparingTo(new BigDecimal("75.00"));
    }

    @Test
    @DisplayName("lanzar SupplierNotFoundException si el proveedor no existe")
    void shouldThrowExceptionWhenSupplierDoesNotExist() {
        UUID unknownSupplier = UUID.randomUUID();
        ReceiveGoodsCommand command = new ReceiveGoodsCommand(unknownSupplier, "ALB-1234", List.of(line(prawns.getId(), null)));

        assertThatThrownBy(() -> receiveGoods.execute(command))
                .isInstanceOf(SupplierNotFoundException.class);
        assertThat(goodsReceiptRepository.savedBatches()).isEmpty();
    }

    @Test
    @DisplayName("lanzar ProductNotFoundException si algún producto no existe, sin guardar nada")
    void shouldThrowExceptionWhenAProductDoesNotExist() {
        UUID unknownProduct = UUID.randomUUID();
        ReceiveGoodsCommand command = new ReceiveGoodsCommand(supplier.getId(), "ALB-1234",
                List.of(line(prawns.getId(), null), line(unknownProduct, null)));

        assertThatThrownBy(() -> receiveGoods.execute(command))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product not found: " + unknownProduct);
        assertThat(goodsReceiptRepository.savedBatches()).isEmpty();
    }

    @Test
    @DisplayName("rechazar un número de albarán que el proveedor ya tiene, sin guardar nada")
    void shouldRejectDuplicatedDeliveryNoteNumber() {
        receiveGoods.execute(new ReceiveGoodsCommand(supplier.getId(), "ALB-1234", List.of(line(prawns.getId(), null))));
        ReceiveGoodsCommand duplicated = new ReceiveGoodsCommand(supplier.getId(), " ALB-1234 ", List.of(line(hake.getId(), null)));

        assertThatThrownBy(() -> receiveGoods.execute(duplicated))
                .isInstanceOf(DuplicateDeliveryNoteException.class)
                .hasMessage("Supplier already has a delivery note ALB-1234");
        assertThat(goodsReceiptRepository.savedBatches()).hasSize(1);
    }

    @Test
    @DisplayName("no guardar nada si el albarán no es válido")
    void shouldNotSaveAnInvalidReceipt() {
        ReceiveGoodsCommand withoutLines = new ReceiveGoodsCommand(supplier.getId(), "ALB-1234", List.of());

        assertThatThrownBy(() -> receiveGoods.execute(withoutLines))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("A goods receipt needs at least one line");
        assertThat(goodsReceiptRepository.savedBatches()).isEmpty();
    }
}
