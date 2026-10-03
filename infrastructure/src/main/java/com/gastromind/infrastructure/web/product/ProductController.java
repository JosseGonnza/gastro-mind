package com.gastromind.infrastructure.web.product;

import com.gastromind.application.product.CreateProduct;
import com.gastromind.application.product.GetProduct;
import com.gastromind.domain.entity.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final CreateProduct createProduct;
    private final GetProduct getProduct;

    public ProductController(CreateProduct createProduct, GetProduct getProduct) {
        this.createProduct = createProduct;
        this.getProduct = getProduct;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@RequestBody CreateProductRequest request) {
        Product product = createProduct.execute(request.toCommand());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(product.getId())
                .toUri();
        return ResponseEntity.created(location).body(ProductResponse.from(product));
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable UUID id) {
        return ProductResponse.from(getProduct.execute(id));
    }
}
