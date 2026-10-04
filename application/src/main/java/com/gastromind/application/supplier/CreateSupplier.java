package com.gastromind.application.supplier;

import com.gastromind.domain.entity.Supplier;

public class CreateSupplier {

    private final SupplierRepository supplierRepository;

    public CreateSupplier(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public Supplier execute(CreateSupplierCommand command) {
        Supplier supplier = Supplier.create(command.name(), command.taxId(), command.phone(), command.email());
        supplierRepository.save(supplier);
        return supplier;
    }
}
