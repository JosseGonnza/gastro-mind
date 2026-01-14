package com.gastromind.domain.entity;

import com.gastromind.domain.valueobject.Allergen;
import com.gastromind.domain.valueobject.Category;
import com.gastromind.domain.valueobject.UnitOfMeasure;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Product debería")
public class ProductTest {

    @Test
    @DisplayName("Crear un producto válido")
    void shouldCreateValidProduct() {
        String name = "Arroz Bomba";
        String description = "Arroz especial para paellas";
        Category category = Category.GRAIN;
        UnitOfMeasure unit = UnitOfMeasure.KILOGRAM;
        //Set no tiene duplicidades, mejor que List para manejar la unicidad
        Set<Allergen> allergens = Set.of();

        Product product = Product.create(
                name,
                description,
                category,
                unit,
                allergens);

        assertThat(product.getId()).isNotNull();
        assertThat(product.getName()).isEqualTo(name);
        assertThat(product.getDescription()).isEqualTo(description);
        assertThat(product.getCategory()).isEqualTo(category);
        assertThat(product.getUnit()).isEqualTo(unit);
        assertThat(product.getAllergens()).isEmpty();
    }
}
