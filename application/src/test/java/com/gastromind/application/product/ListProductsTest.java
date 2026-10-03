package com.gastromind.application.product;

import com.gastromind.domain.entity.Product;
import com.gastromind.domain.valueobject.Category;
import com.gastromind.domain.valueobject.UnitOfMeasure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ListProducts debería")
class ListProductsTest {

    private InMemoryProductRepository productRepository;
    private ListProducts listProducts;

    @BeforeEach
    void setUp() {
        productRepository = new InMemoryProductRepository();
        listProducts = new ListProducts(productRepository);
    }

    @Test
    @DisplayName("devolver los productos tal como los entrega el repositorio")
    void shouldReturnProductsFromRepository() {
        Product rice = Product.create("Arroz bomba", "Especial paella", Category.GRAIN, UnitOfMeasure.KILOGRAM, Set.of());
        Product tomato = Product.create("Tomate", "Tomate pera", Category.VEGETABLE, UnitOfMeasure.KILOGRAM, Set.of());
        productRepository.save(rice);
        productRepository.save(tomato);

        List<Product> products = listProducts.execute();

        assertThat(products).containsExactly(rice, tomato);
    }
}
