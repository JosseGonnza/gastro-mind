package com.gastromind.application.stock;

import com.gastromind.application.product.InMemoryProductRepository;
import com.gastromind.domain.entity.Batch;
import com.gastromind.domain.entity.Product;
import com.gastromind.domain.valueobject.Category;
import com.gastromind.domain.valueobject.Money;
import com.gastromind.domain.valueobject.Quantity;
import com.gastromind.domain.valueobject.UnitOfMeasure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GetStock debería")
class GetStockTest {

    private InMemoryProductRepository productRepository;
    private InMemoryBatchRepository batchRepository;
    private GetStock getStock;
    private Product rice;
    private Product hake;
    private Product tomato;

    @BeforeEach
    void setUp() {
        productRepository = new InMemoryProductRepository();
        batchRepository = new InMemoryBatchRepository();
        getStock = new GetStock(productRepository, batchRepository);
        rice = Product.create("Arroz bomba", null, Category.GRAIN, UnitOfMeasure.KILOGRAM, Set.of());
        hake = Product.create("Merluza de pincho", null, Category.FISH, UnitOfMeasure.KILOGRAM, Set.of());
        tomato = Product.create("Tomate pera", null, Category.VEGETABLE, UnitOfMeasure.KILOGRAM, Set.of());
        productRepository.save(rice);
        productRepository.save(hake);
        productRepository.save(tomato);
    }

    private Batch batch(Product product, double kilograms, int daysToExpire) {
        return Batch.create(product, "LOT-" + UUID.randomUUID(), LocalDate.now().plusDays(daysToExpire),
                Money.of(10.0), Quantity.of(kilograms, UnitOfMeasure.KILOGRAM));
    }

    @Test
    @DisplayName("agrupar los lotes con existencias por producto, con su total y por caducidad")
    void shouldGroupAvailableBatchesByProduct() {
        Batch riceBatch = batch(rice, 5, 30);
        Batch freshHake = batch(hake, 1, 5);
        Batch hakeExpiringFirst = batch(hake, 3, 2);
        batchRepository.add(riceBatch, freshHake, hakeExpiringFirst);

        List<ProductStock> stock = getStock.execute();

        assertThat(stock).extracting(ProductStock::product).containsExactly(rice, hake);
        assertThat(stock.get(0).total()).isEqualTo(Quantity.of(5, UnitOfMeasure.KILOGRAM));
        assertThat(stock.get(1).total()).isEqualTo(Quantity.of(4, UnitOfMeasure.KILOGRAM));
        assertThat(stock.get(1).batches()).containsExactly(hakeExpiringFirst, freshHake);
    }

    @Test
    @DisplayName("no incluir los productos sin existencias")
    void shouldNotIncludeProductsWithoutStock() {
        Batch spent = Batch.restore(UUID.randomUUID(), hake.getId(), UnitOfMeasure.KILOGRAM, "ME-11", LocalDate.now(),
                LocalDate.now().plusDays(3), Money.of(15.0), Quantity.of(1, UnitOfMeasure.KILOGRAM),
                Quantity.of(0, UnitOfMeasure.KILOGRAM));
        batchRepository.add(batch(rice, 5, 30), spent);

        assertThat(getStock.execute()).extracting(ProductStock::product).containsExactly(rice);
    }
}
