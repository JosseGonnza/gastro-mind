package com.gastromind.application.product;

import com.gastromind.domain.entity.Product;

public class CreateProduct {

    private final ProductRepository productRepository;

    public CreateProduct(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product execute(CreateProductCommand command) {
        Product product = Product.create(
                command.name(),
                command.description(),
                command.category(),
                command.unit(),
                command.allergens()
        );
        productRepository.save(product);
        return product;
    }
}
