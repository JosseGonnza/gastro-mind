package com.gastromind.application.product;

import com.gastromind.domain.entity.Product;
import com.gastromind.domain.valueobject.Category;
import com.gastromind.domain.valueobject.UnitOfMeasure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GetProduct debería")
class GetProductTest {

    private InMemoryProductRepository productRepository;
    private GetProduct getProduct;

    @BeforeEach
    void setUp() {
        productRepository = new InMemoryProductRepository();
        getProduct = new GetProduct(productRepository);
    }

    @Test
    @DisplayName("devolver el producto si existe")
    void shouldReturnProductWhenItExists() {
        Product rice = Product.create("Arroz bomba", "Especial paella", Category.GRAIN, UnitOfMeasure.KILOGRAM, Set.of());
        productRepository.save(rice);

        Product found = getProduct.execute(rice.getId());

        assertThat(found).isSameAs(rice);
    }

    @Test
    @DisplayName("lanzar ProductNotFoundException si el producto no existe")
    void shouldThrowExceptionWhenProductDoesNotExist() {
        UUID unknownId = UUID.randomUUID();

        assertThatThrownBy(() -> getProduct.execute(unknownId))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining(unknownId.toString());
    }
}
