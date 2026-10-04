package com.gastromind.application.supplier;

import com.gastromind.domain.entity.Supplier;

import java.util.UUID;

public class GetSupplier {

    private final SupplierRepository supplierRepository;

    public GetSupplier(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public Supplier execute(UUID id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));
    }
}
