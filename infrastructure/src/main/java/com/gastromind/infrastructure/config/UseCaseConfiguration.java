package com.gastromind.infrastructure.config;

import com.gastromind.application.product.CreateProduct;
import com.gastromind.application.product.GetProduct;
import com.gastromind.application.product.ProductRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfiguration {

    @Bean
    CreateProduct createProduct(ProductRepository productRepository) {
        return new CreateProduct(productRepository);
    }

    @Bean
    GetProduct getProduct(ProductRepository productRepository) {
        return new GetProduct(productRepository);
    }
}
