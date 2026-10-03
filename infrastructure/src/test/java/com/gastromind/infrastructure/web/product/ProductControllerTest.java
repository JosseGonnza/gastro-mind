package com.gastromind.infrastructure.web.product;

import com.gastromind.infrastructure.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@DisplayName("La API de productos debería")
class ProductControllerTest {

    private static final String BREAD_JSON = """
            {
              "name": "Pan de semillas",
              "description": "Pan con sésamo",
              "category": "BAKERY",
              "unit": "UNIT",
              "allergens": ["SESAME", "GLUTEN"]
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void cleanDatabase() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM product");
        }
    }

    @Test
    @DisplayName("crear un producto y responder 201 con su ubicación")
    void shouldCreateProduct() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BREAD_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/products/[0-9a-f-]{36}")))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Pan de semillas"))
                .andExpect(jsonPath("$.category").value("BAKERY"))
                .andExpect(jsonPath("$.unit").value("UNIT"))
                .andExpect(jsonPath("$.allergens", containsInAnyOrder("GLUTEN", "SESAME")));
    }

    @Test
    @DisplayName("consultar un producto por su id")
    void shouldGetProductById() throws Exception {
        String location = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BREAD_JSON))
                .andReturn()
                .getResponse()
                .getHeader("Location");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pan de semillas"))
                .andExpect(jsonPath("$.allergens", contains("GLUTEN", "SESAME")));
    }

    @Test
    @DisplayName("responder 404 si el producto no existe")
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        UUID unknownId = UUID.randomUUID();

        mockMvc.perform(get("/products/{id}", unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Product not found: " + unknownId));
    }

    @Test
    @DisplayName("listar los productos ordenados por nombre")
    void shouldListProductsSortedByName() throws Exception {
        createProduct("Tomate", "VEGETABLE", "KILOGRAM");
        createProduct("Ñora", "SPICE", "UNIT");
        createProduct("aceite de oliva", "SAUCE", "LITER");

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", contains("aceite de oliva", "Ñora", "Tomate")));
    }

    private void createProduct(String name, String category, String unit) throws Exception {
        String json = """
                {"name": "%s", "category": "%s", "unit": "%s"}
                """.formatted(name, category, unit);
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }
}
