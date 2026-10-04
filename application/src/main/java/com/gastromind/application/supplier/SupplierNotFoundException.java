package com.gastromind.application.supplier;

import java.util.UUID;

public class SupplierNotFoundException extends RuntimeException {

    private final UUID supplierId;

    public SupplierNotFoundException(UUID supplierId) {
        super("Supplier not found: " + supplierId);
        this.supplierId = supplierId;
    }

    public UUID getSupplierId() {
        return supplierId;
    }
}
