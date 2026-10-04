package com.gastromind.application.supplier;

public record CreateSupplierCommand(String name, String taxId, String phone, String email) {
}
