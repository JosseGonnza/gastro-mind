package com.gastromind.application.product;

import com.gastromind.domain.entity.Product;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class InMemoryProductRepository implements ProductRepository {

    private final Map<UUID, Product> products = new LinkedHashMap<>();

    @Override
    public void save(Product product) {
        products.put(product.getId(), product);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return Optional.ofNullable(products.get(id));
    }

    @Override
    public List<Product> findAll() {
        return List.copyOf(products.values());
    }

    @Override
    public List<Product> findAllById(Collection<UUID> ids) {
        return ids.stream()
                .distinct()
                .map(products::get)
                .filter(Objects::nonNull)
                .toList();
    }
}
