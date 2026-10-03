package com.gastromind.application.product;

import com.gastromind.domain.entity.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    void save(Product product);

    Optional<Product> findById(UUID id);

    List<Product> findAll();
}
