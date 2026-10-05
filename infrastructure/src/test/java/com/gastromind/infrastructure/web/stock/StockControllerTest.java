package com.gastromind.infrastructure.web.stock;

import com.gastromind.application.product.ProductRepository;
import com.gastromind.application.receipt.GoodsReceiptRepository;
import com.gastromind.application.supplier.SupplierRepository;
import com.gastromind.domain.entity.GoodsReceipt;
import com.gastromind.domain.entity.Product;
import com.gastromind.domain.entity.Supplier;
import com.gastromind.domain.valueobject.Allergen;
import com.gastromind.domain.valueobject.Category;
import com.gastromind.domain.valueobject.Money;
import com.gastromind.domain.valueobject.NewReceiptLine;
import com.gastromind.domain.valueobject.Quantity;
import com.gastromind.domain.valueobject.UnitOfMeasure;
import com.gastromind.infrastructure.DatabaseCleaner;
import com.gastromind.infrastructure.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@DisplayName("La API de stock debería")
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private GoodsReceiptRepository goodsReceiptRepository;

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseCleaner.clean(dataSource);
    }

    @Test
    @DisplayName("mostrar qué hay de cada producto: total y lotes, primero los que caducan antes")
    void shouldShowStockByProduct() throws Exception {
        Supplier supplier = Supplier.create("Pescados Cimadevilla", null, null, null);
        Product prawns = Product.create("Gamba roja", null, Category.SEAFOOD, UnitOfMeasure.KILOGRAM, Set.of(Allergen.CRUSTACEANS));
        Product hake = Product.create("Merluza de pincho", null, Category.FISH, UnitOfMeasure.KILOGRAM, Set.of(Allergen.FISH));
        Product tomato = Product.create("Tomate pera", null, Category.VEGETABLE, UnitOfMeasure.KILOGRAM, Set.of());
        supplierRepository.save(supplier);
        productRepository.save(prawns);
        productRepository.save(hake);
        productRepository.save(tomato);
        goodsReceiptRepository.save(GoodsReceipt.register(supplier, "ALB-1234", List.of(
                new NewReceiptLine(hake, Quantity.of(1, UnitOfMeasure.KILOGRAM), Money.of(15.0), LocalDate.now().plusDays(5), "ME-13"),
                new NewReceiptLine(prawns, Quantity.of(2, UnitOfMeasure.KILOGRAM), Money.of(60.0), LocalDate.now().plusDays(4), "GR-778"),
                new NewReceiptLine(hake, Quantity.of(3, UnitOfMeasure.KILOGRAM), Money.of(45.0), LocalDate.now().plusDays(2), "ME-12")
        )));

        mockMvc.perform(get("/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", contains("Gamba roja", "Merluza de pincho")))
                .andExpect(jsonPath("$[0].total").value(2))
                .andExpect(jsonPath("$[0].unit").value("KILOGRAM"))
                .andExpect(jsonPath("$[0].allergens", contains("CRUSTACEANS")))
                .andExpect(jsonPath("$[1].total").value(4))
                .andExpect(jsonPath("$[1].batches", hasSize(2)))
                .andExpect(jsonPath("$[1].batches[*].lotCode", contains("ME-12", "ME-13")))
                .andExpect(jsonPath("$[1].batches[0].quantity").value(3))
                .andExpect(jsonPath("$[1].batches[0].expirationDate").value(LocalDate.now().plusDays(2).toString()));
    }

    @Test
    @DisplayName("devolver una lista vacía si no hay existencias")
    void shouldReturnEmptyListWhenThereIsNoStock() throws Exception {
        mockMvc.perform(get("/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
