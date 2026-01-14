package com.gastromind.domain.entity;

import com.gastromind.domain.valueobject.Allergen;
import com.gastromind.domain.valueobject.Category;
import com.gastromind.domain.valueobject.UnitOfMeasure;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Product debería")
public class ProductTest {

    private static final UUID VALID_ID = UUID.randomUUID();
    private static final String VALID_NAME = "Arroz Bomba";
    private static final String VALID_DESC = "Especial paella";
    private static final Category VALID_CATEGORY = Category.GRAIN;
    private static final UnitOfMeasure VALID_UNIT = UnitOfMeasure.KILOGRAM;
    private static final Set<Allergen> NO_ALLERGENS = Set.of();

    @Test
    @DisplayName("Crear un producto válido usando FactoryMethod")
    void shouldCreateValidProduct() {
        Product product = Product.create(
                VALID_NAME,
                VALID_DESC,
                VALID_CATEGORY,
                VALID_UNIT,
                NO_ALLERGENS);

        assertThat(product.getId()).isNotNull();
        assertThat(product.getName()).isEqualTo(VALID_NAME);
        assertThat(product.getDescription()).isEqualTo(VALID_DESC);
        assertThat(product.getCategory()).isEqualTo(VALID_CATEGORY);
        assertThat(product.getUnit()).isEqualTo(VALID_UNIT);
        assertThat(product.getAllergens()).isEmpty();
    }

    @Test
    @DisplayName("Permitir añadir alérgenos")
    void shouldAllowAddAllergens() {
        var allergens = Set.of(Allergen.GLUTEN);
        var product = new Product(
                VALID_ID,
                VALID_NAME,
                VALID_DESC,
                VALID_CATEGORY,
                VALID_UNIT,
                allergens);

        assertThat(product.getAllergens()).contains(Allergen.GLUTEN);
    }

    @Nested
    @DisplayName("(Invariantes)")
    class Validations {

        @Test
        @DisplayName("No permitir ID nulo")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> new Product(
                    null,
                    VALID_NAME,
                    VALID_DESC,
                    VALID_CATEGORY,
                    VALID_UNIT,
                    NO_ALLERGENS))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Product id cannot be null");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "\t", "\n"})
        @DisplayName("No permitir el nombre vacío")
        void shouldThrowExceptionWhenNameIsEmpty(String invalidName) {
            assertThatThrownBy(() -> Product.create(
                    invalidName,
                    VALID_DESC,
                    VALID_CATEGORY,
                    VALID_UNIT,
                    NO_ALLERGENS))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Product name cannot be empty");
        }

        @Test
        @DisplayName("No permitir categoria nula")
        void shouldThrowExceptionWhenCategoryIsNull() {
            assertThatThrownBy(() -> Product.create(
                    VALID_NAME,
                    VALID_DESC,
                    null,
                    VALID_UNIT,
                    NO_ALLERGENS))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Category cannot be null");
        }

        @Test
        @DisplayName("No permitir unidad de medida nula")
        void shouldThrowExceptionWhenUnitIsNula() {
            assertThatThrownBy(() -> Product.create(
                    VALID_NAME,
                    VALID_DESC,
                    VALID_CATEGORY,
                    null,
                    NO_ALLERGENS))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Unit cannot be null");
        }
    }
}
