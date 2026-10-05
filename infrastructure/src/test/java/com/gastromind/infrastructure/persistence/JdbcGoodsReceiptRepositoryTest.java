package com.gastromind.infrastructure.persistence;

import com.gastromind.application.product.ProductRepository;
import com.gastromind.application.receipt.DuplicateDeliveryNoteException;
import com.gastromind.application.receipt.GoodsReceiptRepository;
import com.gastromind.application.receipt.GoodsReceiptSummary;
import com.gastromind.application.receipt.ReceiptFilter;
import com.gastromind.application.supplier.SupplierRepository;
import com.gastromind.domain.entity.Batch;
import com.gastromind.domain.entity.GoodsReceipt;
import com.gastromind.domain.entity.Product;
import com.gastromind.domain.entity.Supplier;
import com.gastromind.domain.valueobject.Allergen;
import com.gastromind.domain.valueobject.Category;
import com.gastromind.domain.valueobject.Money;
import com.gastromind.domain.valueobject.NewReceiptLine;
import com.gastromind.domain.valueobject.Quantity;
import com.gastromind.domain.valueobject.ReceivedGoods;
import com.gastromind.domain.valueobject.UnitOfMeasure;
import com.gastromind.infrastructure.DatabaseCleaner;
import com.gastromind.infrastructure.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@DisplayName("JdbcGoodsReceiptRepository debería")
class JdbcGoodsReceiptRepositoryTest {

    @Autowired
    private GoodsReceiptRepository goodsReceiptRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private DataSource dataSource;

    private Supplier supplier;
    private Product prawns;
    private Product hake;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseCleaner.clean(dataSource);
        supplier = Supplier.create("Pescados Cimadevilla", null, null, null);
        prawns = Product.create("Gamba roja", null, Category.SEAFOOD, UnitOfMeasure.KILOGRAM, Set.of(Allergen.CRUSTACEANS));
        hake = Product.create("Merluza de pincho", null, Category.FISH, UnitOfMeasure.KILOGRAM, Set.of(Allergen.FISH));
        supplierRepository.save(supplier);
        productRepository.save(prawns);
        productRepository.save(hake);
    }

    private ReceivedGoods receive(String deliveryNoteNumber, Product... products) {
        List<NewReceiptLine> lines = Arrays.stream(products)
                .map(product -> new NewReceiptLine(product, Quantity.of(2500, UnitOfMeasure.GRAM), Money.of(37.5),
                        LocalDate.now().plusDays(3), null))
                .toList();
        return GoodsReceipt.register(supplier, deliveryNoteNumber, lines);
    }

    private ReceivedGoods receiveOn(Supplier from, String deliveryNoteNumber, LocalDate receivedOn, Product... products) {
        List<Batch> batches = Arrays.stream(products)
                .map(product -> Batch.restore(UUID.randomUUID(), product.getId(), UnitOfMeasure.KILOGRAM,
                        deliveryNoteNumber + "-1", receivedOn, receivedOn.plusDays(3), Money.of(37.5),
                        Quantity.of(2.5, UnitOfMeasure.KILOGRAM), Quantity.of(2.5, UnitOfMeasure.KILOGRAM)))
                .toList();
        GoodsReceipt receipt = GoodsReceipt.restore(UUID.randomUUID(), from.getId(), deliveryNoteNumber, receivedOn, batches);
        return new ReceivedGoods(receipt, batches);
    }

    @Test
    @DisplayName("guardar un albarán con sus lotes y recuperarlo por su id")
    void shouldSaveAndFindReceiptById() {
        ReceivedGoods received = receive("ALB-1234", prawns, hake);

        goodsReceiptRepository.save(received);

        GoodsReceipt found = goodsReceiptRepository.findById(received.receipt().getId()).orElseThrow();
        assertThat(found).usingRecursiveComparison().isEqualTo(received.receipt());
    }

    @Test
    @DisplayName("devolver vacío si el albarán no existe")
    void shouldReturnEmptyWhenReceiptDoesNotExist() {
        assertThat(goodsReceiptRepository.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    @DisplayName("saber si un proveedor ya tiene un albarán con ese número")
    void shouldKnowWhetherASupplierAlreadyHasADeliveryNote() {
        goodsReceiptRepository.save(receive("ALB-1234", prawns));

        assertThat(goodsReceiptRepository.existsBySupplierAndDeliveryNoteNumber(supplier.getId(), "ALB-1234")).isTrue();
        assertThat(goodsReceiptRepository.existsBySupplierAndDeliveryNoteNumber(supplier.getId(), "ALB-9999")).isFalse();
        assertThat(goodsReceiptRepository.existsBySupplierAndDeliveryNoteNumber(UUID.randomUUID(), "ALB-1234")).isFalse();
    }

    @Test
    @DisplayName("no guardar nada si falla algún lote: ni el albarán ni los lotes anteriores")
    void shouldSaveNothingWhenABatchFails() throws SQLException {
        Product notInDatabase = Product.create("Rape", null, Category.FISH, UnitOfMeasure.KILOGRAM, Set.of());
        ReceivedGoods received = receive("ALB-1234", prawns, notInDatabase);

        assertThatThrownBy(() -> goodsReceiptRepository.save(received))
                .isInstanceOf(RepositoryException.class);
        assertThat(goodsReceiptRepository.findById(received.receipt().getId())).isEmpty();
        assertThat(countRows("goods_receipt")).isZero();
        assertThat(countRows("batch")).isZero();
    }

    @Test
    @DisplayName("rechazar un número de albarán repetido del mismo proveedor, aunque se cuele la comprobación previa")
    void shouldRejectDuplicatedDeliveryNoteNumberForTheSameSupplier() throws SQLException {
        goodsReceiptRepository.save(receive("ALB-1234", prawns));

        //Simula dos registros a la vez: los dos pasan el "¿ya existe?" y es la base de datos quien frena al segundo
        assertThatThrownBy(() -> goodsReceiptRepository.save(receive("ALB-1234", hake)))
                .isInstanceOf(DuplicateDeliveryNoteException.class)
                .hasMessage("Supplier already has a delivery note ALB-1234");
        assertThat(countRows("batch")).isEqualTo(1);
    }

    @Test
    @DisplayName("resumir cada albarán con su proveedor, sus líneas y su total, primero los más recientes")
    void shouldSummarizeReceiptsNewestFirst() {
        LocalDate today = LocalDate.now();
        ReceivedGoods older = receiveOn(supplier, "ALB-1", today.minusDays(7), prawns, hake);
        ReceivedGoods newer = receiveOn(supplier, "ALB-2", today, prawns);
        goodsReceiptRepository.save(older);
        goodsReceiptRepository.save(newer);

        List<GoodsReceiptSummary> summaries = goodsReceiptRepository.findSummaries(ReceiptFilter.all());

        assertThat(summaries).containsExactly(
                new GoodsReceiptSummary(newer.receipt().getId(), supplier.getId(), "Pescados Cimadevilla", "ALB-2",
                        today, 1, new BigDecimal("37.50")),
                new GoodsReceiptSummary(older.receipt().getId(), supplier.getId(), "Pescados Cimadevilla", "ALB-1",
                        today.minusDays(7), 2, new BigDecimal("75.00")));
    }

    @Test
    @DisplayName("filtrar por proveedor y por fechas, con los dos extremos incluidos")
    void shouldFilterBySupplierAndDateRange() {
        Supplier otherSupplier = Supplier.create("Mariscos El Muelle", null, null, null);
        supplierRepository.save(otherSupplier);
        LocalDate today = LocalDate.now();
        goodsReceiptRepository.save(receiveOn(supplier, "ALB-1", today.minusDays(10), prawns));
        goodsReceiptRepository.save(receiveOn(supplier, "ALB-2", today.minusDays(5), prawns));
        goodsReceiptRepository.save(receiveOn(supplier, "ALB-3", today, prawns));
        goodsReceiptRepository.save(receiveOn(otherSupplier, "F-1", today.minusDays(5), hake));

        assertThat(goodsReceiptRepository.findSummaries(new ReceiptFilter(supplier.getId(), today.minusDays(5), today)))
                .extracting(GoodsReceiptSummary::deliveryNoteNumber)
                .containsExactly("ALB-3", "ALB-2");
        assertThat(goodsReceiptRepository.findSummaries(new ReceiptFilter(null, today.minusDays(5), today.minusDays(5))))
                .extracting(GoodsReceiptSummary::deliveryNoteNumber)
                .containsExactlyInAnyOrder("ALB-2", "F-1");
    }

    private int countRows(String table) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }
}
