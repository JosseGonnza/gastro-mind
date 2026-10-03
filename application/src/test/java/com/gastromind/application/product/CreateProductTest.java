package com.gastromind.application.product;

import com.gastromind.domain.entity.Product;
import com.gastromind.domain.valueobject.Allergen;
import com.gastromind.domain.valueobject.Category;
import com.gastromind.domain.valueobject.UnitOfMeasure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CreateProduct debería")
class CreateProductTest {

    private InMemoryProductRepository productRepository;
    private CreateProduct createProduct;

    @BeforeEach
    void setUp() {
        productRepository = new InMemoryProductRepository();
        createProduct = new CreateProduct(productRepository);
    }

    @Test
    @DisplayName("crear un producto y guardarlo")
    void shouldCreateAndSaveProduct() {
        CreateProductCommand command = new CreateProductCommand(
                "Pan de semillas",
                "Pan con sésamo",
                Category.BAKERY,
                UnitOfMeasure.UNIT,
                Set.of(Allergen.GLUTEN, Allergen.SESAME)
        );

        Product created = createProduct.execute(command);

        assertThat(created.getName()).isEqualTo("Pan de semillas");
        assertThat(created.getAllergens()).containsExactlyInAnyOrder(Allergen.GLUTEN, Allergen.SESAME);
        assertThat(productRepository.findById(created.getId())).containsSame(created);
    }
}
