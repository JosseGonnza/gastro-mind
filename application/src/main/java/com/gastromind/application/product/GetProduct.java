package com.gastromind.application.product;

import com.gastromind.domain.entity.Product;

import java.util.UUID;

public class GetProduct {

    private final ProductRepository productRepository;

    public GetProduct(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product execute(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
