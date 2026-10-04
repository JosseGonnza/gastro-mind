package com.gastromind.domain.entity;

import com.gastromind.domain.exception.DomainValidationException;

import java.util.UUID;

public class Supplier {
    private final UUID id;
    private final String name;
    private final String taxId;
    private final String phone;
    private final String email;

    public Supplier(UUID id, String name, String taxId, String phone, String email) {
        if (id == null) throw new DomainValidationException("Supplier id cannot be null");
        if (name == null || name.isBlank()) throw new DomainValidationException("Supplier name cannot be empty");
        String cleanEmail = blankToNull(email);
        if (cleanEmail != null && !cleanEmail.contains("@")) {
            throw new DomainValidationException("Supplier email is not valid");
        }
        this.id = id;
        this.name = name.trim();
        this.taxId = blankToNull(taxId);
        this.phone = blankToNull(phone);
        this.email = cleanEmail;
    }

    public static Supplier create(String name, String taxId, String phone, String email) {
        return new Supplier(UUID.randomUUID(), name, taxId, phone, email);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTaxId() {
        return taxId;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }
}
