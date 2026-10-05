package com.gastromind.infrastructure.web.supplier;

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

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.UUID;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@DisplayName("La API de proveedores debería")
class SupplierControllerTest {

    private static final String FISH_SUPPLIER_JSON = """
            {
              "name": "Pescados Cimadevilla",
              "taxId": "B33123456",
              "phone": "985 123 456",
              "email": "pedidos@cimadevilla.es"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void cleanDatabase() throws SQLException {
        DatabaseCleaner.clean(dataSource);
    }

    @Test
    @DisplayName("crear un proveedor y responder 201 con su ubicación")
    void shouldCreateSupplier() throws Exception {
        mockMvc.perform(post("/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(FISH_SUPPLIER_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/suppliers/[0-9a-f-]{36}")))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Pescados Cimadevilla"))
                .andExpect(jsonPath("$.taxId").value("B33123456"))
                .andExpect(jsonPath("$.email").value("pedidos@cimadevilla.es"));
    }

    @Test
    @DisplayName("consultar un proveedor por su id")
    void shouldGetSupplierById() throws Exception {
        String location = mockMvc.perform(post("/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(FISH_SUPPLIER_JSON))
                .andReturn()
                .getResponse()
                .getHeader("Location");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pescados Cimadevilla"))
                .andExpect(jsonPath("$.phone").value("985 123 456"));
    }

    @Test
    @DisplayName("responder 404 si el proveedor no existe")
    void shouldReturnNotFoundWhenSupplierDoesNotExist() throws Exception {
        UUID unknownId = UUID.randomUUID();

        mockMvc.perform(get("/suppliers/{id}", unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Supplier not found: " + unknownId));
    }

    @Test
    @DisplayName("listar los proveedores ordenados por nombre")
    void shouldListSuppliersSortedByName() throws Exception {
        createSupplier("Pescados Cimadevilla");
        createSupplier("Óleos del Norte");
        createSupplier("frutas Pepe");

        mockMvc.perform(get("/suppliers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", contains("frutas Pepe", "Óleos del Norte", "Pescados Cimadevilla")));
    }

    @Test
    @DisplayName("responder 400 si el proveedor no es válido")
    void shouldReturnBadRequestWhenSupplierIsInvalid() throws Exception {
        mockMvc.perform(post("/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": " "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Supplier name cannot be empty"));
    }

    private void createSupplier(String name) throws Exception {
        mockMvc.perform(post("/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "%s"}
                                """.formatted(name)))
                .andExpect(status().isCreated());
    }
}
