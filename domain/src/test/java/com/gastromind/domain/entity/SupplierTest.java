package com.gastromind.domain.entity;

import com.gastromind.domain.exception.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Supplier debería")
class SupplierTest {

    @Test
    @DisplayName("Crear un proveedor con todos sus datos")
    void shouldCreateSupplier() {
        Supplier supplier = Supplier.create("Pescados Cimadevilla", "B33123456", "985 123 456", "pedidos@cimadevilla.es");

        assertThat(supplier.getId()).isNotNull();
        assertThat(supplier.getName()).isEqualTo("Pescados Cimadevilla");
        assertThat(supplier.getTaxId()).isEqualTo("B33123456");
        assertThat(supplier.getPhone()).isEqualTo("985 123 456");
        assertThat(supplier.getEmail()).isEqualTo("pedidos@cimadevilla.es");
    }

    @Test
    @DisplayName("Crear un proveedor solo con el nombre, quitando los espacios sobrantes")
    void shouldCreateSupplierWithOnlyName() {
        Supplier supplier = Supplier.create("  Frutas Pepe  ", " ", "", null);

        assertThat(supplier.getName()).isEqualTo("Frutas Pepe");
        assertThat(supplier.getTaxId()).isNull();
        assertThat(supplier.getPhone()).isNull();
        assertThat(supplier.getEmail()).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("No permitir un proveedor sin nombre")
    void shouldThrowExceptionWhenNameIsEmpty(String invalidName) {
        assertThatThrownBy(() -> Supplier.create(invalidName, null, null, null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Supplier name cannot be empty");
    }

    @Test
    @DisplayName("No permitir un email sin arroba")
    void shouldThrowExceptionWhenEmailIsInvalid() {
        assertThatThrownBy(() -> Supplier.create("Frutas Pepe", null, null, "frutaspepe.es"))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Supplier email is not valid");
    }

    @Test
    @DisplayName("No permitir un proveedor sin id")
    void shouldThrowExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> new Supplier(null, "Frutas Pepe", null, null, null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("Supplier id cannot be null");
    }

    @Test
    @DisplayName("Reconstruir un proveedor guardado con su id")
    void shouldRestoreSupplierWithItsId() {
        UUID id = UUID.randomUUID();

        Supplier supplier = new Supplier(id, "Frutas Pepe", null, null, null);

        assertThat(supplier.getId()).isEqualTo(id);
    }
}
