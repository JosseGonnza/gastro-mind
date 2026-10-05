package com.gastromind.infrastructure.persistence;

import com.gastromind.application.supplier.SupplierRepository;
import com.gastromind.domain.entity.Supplier;
import com.gastromind.infrastructure.DatabaseCleaner;
import com.gastromind.infrastructure.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@DisplayName("JdbcSupplierRepository debería")
class JdbcSupplierRepositoryTest {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void cleanDatabase() throws SQLException {
        DatabaseCleaner.clean(dataSource);
    }

    @Test
    @DisplayName("guardar un proveedor y recuperarlo por su id")
    void shouldSaveAndFindSupplierById() {
        Supplier supplier = Supplier.create("Pescados Cimadevilla", "B33123456", "985 123 456", "pedidos@cimadevilla.es");

        supplierRepository.save(supplier);

        assertThat(supplierRepository.findById(supplier.getId())).get()
                .usingRecursiveComparison()
                .isEqualTo(supplier);
    }

    @Test
    @DisplayName("guardar un proveedor que solo tiene nombre")
    void shouldSaveSupplierWithOnlyName() {
        Supplier supplier = Supplier.create("Frutas Pepe", null, null, null);

        supplierRepository.save(supplier);

        assertThat(supplierRepository.findById(supplier.getId())).get()
                .usingRecursiveComparison()
                .isEqualTo(supplier);
    }

    @Test
    @DisplayName("devolver vacío si el proveedor no existe")
    void shouldReturnEmptyWhenSupplierDoesNotExist() {
        assertThat(supplierRepository.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    @DisplayName("listar los proveedores ordenados como en español")
    void shouldListSuppliersSortedInSpanish() {
        supplierRepository.save(Supplier.create("Pescados Cimadevilla", null, null, null));
        supplierRepository.save(Supplier.create("Óleos del Norte", null, null, null));
        supplierRepository.save(Supplier.create("frutas Pepe", null, null, null));

        assertThat(supplierRepository.findAll())
                .extracting(Supplier::getName)
                .containsExactly("frutas Pepe", "Óleos del Norte", "Pescados Cimadevilla");
    }

    @Test
    @DisplayName("lanzar una excepción si el proveedor ya existe")
    void shouldThrowExceptionWhenSupplierAlreadyExists() {
        Supplier supplier = Supplier.create("Frutas Pepe", null, null, null);
        supplierRepository.save(supplier);

        assertThatThrownBy(() -> supplierRepository.save(supplier))
                .isInstanceOf(RepositoryException.class)
                .hasMessageContaining(supplier.getId().toString());
    }
}
