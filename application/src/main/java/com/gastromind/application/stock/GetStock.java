package com.gastromind.application.stock;

import com.gastromind.application.product.ProductRepository;
import com.gastromind.domain.entity.Batch;
import com.gastromind.domain.entity.Product;
import com.gastromind.domain.service.InventoryService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GetStock {

    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;
    private final InventoryService inventoryService = new InventoryService();

    public GetStock(ProductRepository productRepository, BatchRepository batchRepository) {
        this.productRepository = productRepository;
        this.batchRepository = batchRepository;
    }

    public List<ProductStock> execute() {
        Map<UUID, List<Batch>> batchesByProduct = batchRepository.findAvailable().stream()
                .collect(Collectors.groupingBy(Batch::getProductId, LinkedHashMap::new, Collectors.toList()));
        return productRepository.findAll().stream()
                .filter(product -> batchesByProduct.containsKey(product.getId()))
                .map(product -> toStock(product, batchesByProduct.get(product.getId())))
                .toList();
    }

    private ProductStock toStock(Product product, List<Batch> batches) {
        return new ProductStock(product, inventoryService.calculateCurrentStock(product, batches), batches);
    }
}
