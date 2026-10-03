package com.gastromind.infrastructure.persistence;

import com.gastromind.application.product.ProductRepository;
import com.gastromind.domain.entity.Product;
import com.gastromind.domain.valueobject.Allergen;
import com.gastromind.domain.valueobject.Category;
import com.gastromind.domain.valueobject.UnitOfMeasure;
import com.gastromind.infrastructure.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@DisplayName("JdbcProductRepository debería")
class JdbcProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

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
}
