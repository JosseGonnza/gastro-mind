package com.gastromind.infrastructure.web.supplier;

import com.gastromind.application.supplier.CreateSupplierCommand;

public record CreateSupplierRequest(String name, String taxId, String phone, String email) {

    public CreateSupplierCommand toCommand() {
        return new CreateSupplierCommand(name, taxId, phone, email);
    }
}
