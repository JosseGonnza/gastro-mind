package com.gastromind.infrastructure.persistence;

import com.gastromind.application.product.ProductRepository;
import com.gastromind.domain.entity.Product;
import com.gastromind.domain.valueobject.Allergen;
import com.gastromind.domain.valueobject.Category;
import com.gastromind.domain.valueobject.UnitOfMeasure;
import com.gastromind.infrastructure.TestcontainersConfiguration;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@DisplayName("JdbcProductRepository debería")
class JdbcProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

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
    @DisplayName("guardar un producto y recuperarlo por su id")
    void shouldSaveAndFindProductById() {
        Product bread = Product.create(
                "Pan de semillas",
                "Pan con sésamo",
                Category.BAKERY,
                UnitOfMeasure.UNIT,
                Set.of(Allergen.GLUTEN, Allergen.SESAME)
        );

        productRepository.save(bread);
        Optional<Product> found = productRepository.findById(bread.getId());

        assertThat(found).get().usingRecursiveComparison().isEqualTo(bread);
    }

    @Test
    @DisplayName("devolver vacío si el producto no existe")
    void shouldReturnEmptyWhenProductDoesNotExist() {
        Optional<Product> found = productRepository.findById(UUID.randomUUID());

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("guardar un producto sin alérgenos")
    void shouldSaveProductWithoutAllergens() {
        Product rice = Product.create(
                "Arroz bomba",
                "Especial paella",
                Category.GRAIN,
                UnitOfMeasure.KILOGRAM,
                Set.of()
        );

        productRepository.save(rice);
        Optional<Product> found = productRepository.findById(rice.getId());

        assertThat(found).get()
                .extracting(Product::getAllergens)
                .asInstanceOf(InstanceOfAssertFactories.collection(Allergen.class))
                .isEmpty();
    }

    @Test
    @DisplayName("lanzar una excepción si el producto ya existe")
    void shouldThrowExceptionWhenProductAlreadyExists() {
        Product rice = Product.create(
                "Arroz bomba",
                "Especial paella",
                Category.GRAIN,
                UnitOfMeasure.KILOGRAM,
                Set.of()
        );
        productRepository.save(rice);

        assertThatThrownBy(() -> productRepository.save(rice))
                .isInstanceOf(RepositoryException.class)
                .hasMessageContaining(rice.getId().toString());
    }

    @Test
    @DisplayName("listar los productos ordenados por nombre, cada uno con sus alérgenos")
    void shouldFindAllProductsSortedByName() {
        Product tomato = Product.create("Tomate", "Tomate pera", Category.VEGETABLE, UnitOfMeasure.KILOGRAM, Set.of());
        Product rice = Product.create("Arroz bomba", "Especial paella", Category.GRAIN, UnitOfMeasure.KILOGRAM, Set.of());
        Product bread = Product.create("Pan de semillas", "Pan con sésamo", Category.BAKERY, UnitOfMeasure.UNIT,
                Set.of(Allergen.GLUTEN, Allergen.SESAME));
        productRepository.save(tomato);
        productRepository.save(rice);
        productRepository.save(bread);

        List<Product> products = productRepository.findAll();

        assertThat(products)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(rice, bread, tomato);
    }

    @Test
    @DisplayName("devolver una lista vacía si no hay productos")
    void shouldReturnEmptyListWhenThereAreNoProducts() {
        List<Product> products = productRepository.findAll();

        assertThat(products).isEmpty();
    }
}
