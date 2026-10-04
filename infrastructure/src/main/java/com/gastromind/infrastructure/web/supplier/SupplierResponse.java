package com.gastromind.infrastructure.web.supplier;

import com.gastromind.domain.entity.Supplier;

import java.util.UUID;

public record SupplierResponse(UUID id, String name, String taxId, String phone, String email) {

    public static SupplierResponse from(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getTaxId(),
                supplier.getPhone(),
                supplier.getEmail()
        );
    }
}
