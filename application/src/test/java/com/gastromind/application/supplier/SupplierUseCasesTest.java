package com.gastromind.application.supplier;

import com.gastromind.domain.entity.Supplier;
import com.gastromind.domain.exception.DomainValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Los casos de uso de proveedores deberían")
class SupplierUseCasesTest {

    private InMemorySupplierRepository supplierRepository;

    @BeforeEach
    void setUp() {
        supplierRepository = new InMemorySupplierRepository();
    }

    @Nested
    @DisplayName("al crear un proveedor")
    class Create {

        @Test
        @DisplayName("crearlo y guardarlo")
        void shouldCreateAndSaveSupplier() {
            CreateSupplier createSupplier = new CreateSupplier(supplierRepository);

            Supplier created = createSupplier.execute(
                    new CreateSupplierCommand("Pescados Cimadevilla", "B33123456", "985 123 456", null));

            assertThat(created.getName()).isEqualTo("Pescados Cimadevilla");
            assertThat(supplierRepository.findById(created.getId())).containsSame(created);
        }

        @Test
        @DisplayName("no guardar nada si el proveedor no es válido")
        void shouldNotSaveInvalidSupplier() {
            CreateSupplier createSupplier = new CreateSupplier(supplierRepository);

            assertThatThrownBy(() -> createSupplier.execute(new CreateSupplierCommand(" ", null, null, null)))
                    .isInstanceOf(DomainValidationException.class);
            assertThat(supplierRepository.findAll()).isEmpty();
        }
    }

    @Nested
    @DisplayName("al consultar un proveedor")
    class Get {

        @Test
        @DisplayName("devolverlo si existe")
        void shouldReturnSupplierWhenItExists() {
            Supplier supplier = Supplier.create("Frutas Pepe", null, null, null);
            supplierRepository.save(supplier);

            assertThat(new GetSupplier(supplierRepository).execute(supplier.getId())).isSameAs(supplier);
        }

        @Test
        @DisplayName("lanzar SupplierNotFoundException si no existe")
        void shouldThrowExceptionWhenSupplierDoesNotExist() {
            UUID unknownId = UUID.randomUUID();

            assertThatThrownBy(() -> new GetSupplier(supplierRepository).execute(unknownId))
                    .isInstanceOf(SupplierNotFoundException.class)
                    .hasMessage("Supplier not found: " + unknownId);
        }
    }

    @Test
    @DisplayName("listar los proveedores tal como los entrega el repositorio")
    void shouldListSuppliers() {
        Supplier fish = Supplier.create("Pescados Cimadevilla", null, null, null);
        Supplier fruit = Supplier.create("Frutas Pepe", null, null, null);
        supplierRepository.save(fish);
        supplierRepository.save(fruit);

        assertThat(new ListSuppliers(supplierRepository).execute()).containsExactly(fish, fruit);
    }
}
