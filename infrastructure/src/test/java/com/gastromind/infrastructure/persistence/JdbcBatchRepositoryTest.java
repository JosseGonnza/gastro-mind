package com.gastromind.infrastructure.persistence;

import com.gastromind.application.product.ProductRepository;
import com.gastromind.application.receipt.GoodsReceiptRepository;
import com.gastromind.application.stock.BatchRepository;
import com.gastromind.application.supplier.SupplierRepository;
import com.gastromind.domain.entity.Batch;
import com.gastromind.domain.entity.GoodsReceipt;
import com.gastromind.domain.entity.Product;
import com.gastromind.domain.entity.Supplier;
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
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@DisplayName("JdbcBatchRepository debería")
class JdbcBatchRepositoryTest {

    @Autowired
    private BatchRepository batchRepository;

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
        prawns = Product.create("Gamba roja", null, Category.SEAFOOD, UnitOfMeasure.KILOGRAM, Set.of());
        hake = Product.create("Merluza de pincho", null, Category.FISH, UnitOfMeasure.KILOGRAM, Set.of());
        supplierRepository.save(supplier);
        productRepository.save(prawns);
        productRepository.save(hake);
    }

    @Test
    @DisplayName("devolver los lotes con existencias, primero los que caducan antes")
    void shouldReturnAvailableBatchesSortedByExpiration() {
        ReceivedGoods received = GoodsReceipt.register(supplier, "ALB-1234", List.of(
                new NewReceiptLine(prawns, Quantity.of(2, UnitOfMeasure.KILOGRAM), Money.of(60.0), LocalDate.now().plusDays(4), "GR-778"),
                new NewReceiptLine(hake, Quantity.of(3, UnitOfMeasure.KILOGRAM), Money.of(45.0), LocalDate.now().plusDays(2), "ME-12")
        ));
        goodsReceiptRepository.save(received);
        Batch prawnsBatch = received.batches().get(0);
        Batch hakeBatch = received.batches().get(1);

        List<Batch> available = batchRepository.findAvailable();

        assertThat(available)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(hakeBatch, prawnsBatch);
    }

    @Test
    @DisplayName("no devolver los lotes ya gastados")
    void shouldNotReturnSpentBatches() {
        Batch spent = Batch.restore(UUID.randomUUID(), hake.getId(), UnitOfMeasure.KILOGRAM, "ME-11", LocalDate.now(),
                LocalDate.now().plusDays(5), Money.of(15.0), Quantity.of(1, UnitOfMeasure.KILOGRAM),
                Quantity.of(0, UnitOfMeasure.KILOGRAM));
        GoodsReceipt receipt = GoodsReceipt.restore(UUID.randomUUID(), supplier.getId(), "ALB-0001", LocalDate.now(), List.of(spent));
        goodsReceiptRepository.save(new ReceivedGoods(receipt, List.of(spent)));

        assertThat(batchRepository.findAvailable()).isEmpty();
    }
}
