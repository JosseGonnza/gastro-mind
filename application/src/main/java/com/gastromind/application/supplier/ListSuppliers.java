package com.gastromind.application.supplier;

import com.gastromind.domain.entity.Supplier;

import java.util.List;

public class ListSuppliers {

    private final SupplierRepository supplierRepository;

    public ListSuppliers(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<Supplier> execute() {
        return supplierRepository.findAll();
    }
}
