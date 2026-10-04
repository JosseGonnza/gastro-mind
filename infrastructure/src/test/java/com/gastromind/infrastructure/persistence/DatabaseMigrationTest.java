package com.gastromind.infrastructure.persistence;

import com.gastromind.infrastructure.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@DisplayName("La base de datos debería")
class DatabaseMigrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("tener la tabla de productos")
    void shouldHaveProductTable() throws SQLException {
        assertThat(tableExists("product")).isTrue();
    }

    @Test
    @DisplayName("tener la tabla de alérgenos de cada producto")
    void shouldHaveProductAllergenTable() throws SQLException {
        assertThat(tableExists("product_allergen")).isTrue();
    }

    @Test
    @DisplayName("tener la tabla de proveedores")
    void shouldHaveSupplierTable() throws SQLException {
        assertThat(tableExists("supplier")).isTrue();
    }

    private boolean tableExists(String tableName) throws SQLException {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM information_schema.tables
                    WHERE table_schema = 'public' AND table_name = ?
                )
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getBoolean(1);
            }
        }
    }
}
