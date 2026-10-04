package com.gastromind.application.supplier;

import com.gastromind.domain.entity.Supplier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

class InMemorySupplierRepository implements SupplierRepository {

    private final Map<UUID, Supplier> suppliers = new LinkedHashMap<>();

    @Override
    public void save(Supplier supplier) {
        suppliers.put(supplier.getId(), supplier);
    }

    @Override
    public Optional<Supplier> findById(UUID id) {
        return Optional.ofNullable(suppliers.get(id));
    }

    @Override
    public List<Supplier> findAll() {
        return List.copyOf(suppliers.values());
    }
}
