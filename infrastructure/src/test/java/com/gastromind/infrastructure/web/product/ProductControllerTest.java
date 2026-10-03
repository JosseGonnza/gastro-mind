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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.matchesPattern;
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
}
