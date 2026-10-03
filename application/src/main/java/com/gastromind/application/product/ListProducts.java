package com.gastromind.application.product;

import com.gastromind.domain.entity.Product;

import java.util.List;

public class ListProducts {

    private final ProductRepository productRepository;

    public ListProducts(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> execute() {
        return productRepository.findAll();
    }
}
