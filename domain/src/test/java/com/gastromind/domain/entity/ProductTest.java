package com.gastromind.domain.entity;

import com.gastromind.domain.valueobject.Allergen;
import com.gastromind.domain.valueobject.Category;
import com.gastromind.domain.valueobject.UnitOfMeasure;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    @DisplayName("No permitir id nulo")
    void shouldThrowExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> new Product(
                null,
                "Arroz",
                "Redondo",
                Category.GRAIN,
                UnitOfMeasure.KILOGRAM,
                Set.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product id cannot be null");
    }

    @Test
    @DisplayName("No permitir el nombre vacío")
    void shouldThrowExceptionWhenNameIsEmpty() {
        assertThatThrownBy(() -> new Product(
                UUID.randomUUID(),
                "",
                "Redondo",
                Category.GRAIN,
                UnitOfMeasure.KILOGRAM,
                Set.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product name cannot be empty");
    }

    @Test
    @DisplayName("No permitir categoria vacía")
    void shouldThrowExceptionWhenCategoryIsEmpty() {
        assertThatThrownBy(() -> new Product(
                UUID.randomUUID(),
                "Arroz",
                "Redondo",
                null,
                UnitOfMeasure.KILOGRAM,
                Set.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Category cannot be null");
    }

    @Test
    @DisplayName("No permitir unidad de medida vacía")
    void shouldThrowExceptionWhenUnitIsEmpty() {
        assertThatThrownBy(() -> new Product(
                UUID.randomUUID(),
                "Arroz",
                "Redondo",
                Category.GRAIN,
                null,
                Set.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unit cannot be null");
    }

    @Test
    @DisplayName("Permitir añadir alérgenos")
    void shouldAllowAddAllergens() {
        var product = new Product(
                UUID.randomUUID(),
                "Pan",
                "De pueblo",
                Category.GRAIN,
                UnitOfMeasure.KILOGRAM,
                Set.of(Allergen.GLUTEN)
        );

        assertThat(product.getAllergens()).contains(Allergen.GLUTEN);
    }
}
