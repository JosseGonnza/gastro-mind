package com.gastromind.infrastructure.web.receipt;

import com.gastromind.application.product.ProductRepository;
import com.gastromind.application.supplier.SupplierRepository;
import com.gastromind.domain.entity.Product;
import com.gastromind.domain.entity.Supplier;
import com.gastromind.domain.valueobject.Category;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@DisplayName("La API de albaranes debería")
class GoodsReceiptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

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

    private String receiptJson(UUID supplierId, String deliveryNoteNumber) {
        return """
                {
                  "supplierId": "%s",
                  "deliveryNoteNumber": "%s",
                  "lines": [
                    {"productId": "%s", "quantity": 2, "unit": "KILOGRAM", "amount": 60.00,
                     "expirationDate": "%s", "lotCode": "GR-778"},
                    {"productId": "%s", "quantity": 3000, "unit": "GRAM", "amount": 45.00,
                     "expirationDate": "%s"}
                  ]
                }
                """.formatted(supplierId, deliveryNoteNumber, prawns.getId(), LocalDate.now().plusDays(4),
                hake.getId(), LocalDate.now().plusDays(2));
    }

    private ResultActions register(String json) throws Exception {
        return mockMvc.perform(post("/goods-receipts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    @Test
    @DisplayName("registrar un albarán y responder 201 con su ubicación, su total y sus líneas")
    void shouldRegisterReceipt() throws Exception {
        register(receiptJson(supplier.getId(), "ALB-1234"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/goods-receipts/[0-9a-f-]{36}")))
                .andExpect(jsonPath("$.supplierId").value(supplier.getId().toString()))
                .andExpect(jsonPath("$.deliveryNoteNumber").value("ALB-1234"))
                .andExpect(jsonPath("$.receivedOn").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.total").value(105.0))
                .andExpect(jsonPath("$.lines", hasSize(2)))
                .andExpect(jsonPath("$.lines[0].lotCode").value("GR-778"))
                .andExpect(jsonPath("$.lines[1].lotCode").value("ALB-1234-2"))
                .andExpect(jsonPath("$.lines[1].quantity").value(3))
                .andExpect(jsonPath("$.lines[1].unit").value("KILOGRAM"))
                .andExpect(jsonPath("$.lines[1].expirationDate").value(LocalDate.now().plusDays(2).toString()));
    }

    @Test
    @DisplayName("consultar un albarán por su id")
    void shouldGetReceiptById() throws Exception {
        String location = register(receiptJson(supplier.getId(), "ALB-1234"))
                .andReturn()
                .getResponse()
                .getHeader("Location");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryNoteNumber").value("ALB-1234"))
                .andExpect(jsonPath("$.lines", hasSize(2)));
    }

    @Test
    @DisplayName("responder 404 si el albarán no existe")
    void shouldReturnNotFoundWhenReceiptDoesNotExist() throws Exception {
        UUID unknownId = UUID.randomUUID();

        mockMvc.perform(get("/goods-receipts/{id}", unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Goods receipt not found: " + unknownId));
    }

    @Test
    @DisplayName("responder 409 si el proveedor ya tiene un albarán con ese número")
    void shouldReturnConflictWhenDeliveryNoteIsDuplicated() throws Exception {
        register(receiptJson(supplier.getId(), "ALB-1234")).andExpect(status().isCreated());

        register(receiptJson(supplier.getId(), "ALB-1234"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Supplier already has a delivery note ALB-1234"));
    }

    @Test
    @DisplayName("responder 400 si el albarán no tiene líneas")
    void shouldReturnBadRequestWhenReceiptHasNoLines() throws Exception {
        register("""
                {"supplierId": "%s", "deliveryNoteNumber": "ALB-1234", "lines": []}
                """.formatted(supplier.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("A goods receipt needs at least one line"));
    }

    @Test
    @DisplayName("responder 404 si el proveedor no existe")
    void shouldReturnNotFoundWhenSupplierDoesNotExist() throws Exception {
        UUID unknownSupplier = UUID.randomUUID();

        register(receiptJson(unknownSupplier, "ALB-1234"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Supplier not found: " + unknownSupplier));
    }
}
