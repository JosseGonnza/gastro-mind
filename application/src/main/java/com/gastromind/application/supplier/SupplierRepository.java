package com.gastromind.application.supplier;

import com.gastromind.domain.entity.Supplier;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupplierRepository {

    void save(Supplier supplier);

    Optional<Supplier> findById(UUID id);

    List<Supplier> findAll();
}
