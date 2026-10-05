package com.gastromind.infrastructure.config;

import com.gastromind.application.product.CreateProduct;
import com.gastromind.application.product.GetProduct;
import com.gastromind.application.product.ListProducts;
import com.gastromind.application.product.ProductRepository;
import com.gastromind.application.receipt.GetGoodsReceipt;
import com.gastromind.application.receipt.GoodsReceiptRepository;
import com.gastromind.application.receipt.ReceiveGoods;
import com.gastromind.application.stock.BatchRepository;
import com.gastromind.application.stock.GetStock;
import com.gastromind.application.supplier.CreateSupplier;
import com.gastromind.application.supplier.GetSupplier;
import com.gastromind.application.supplier.ListSuppliers;
import com.gastromind.application.supplier.SupplierRepository;
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

    @Bean
    ListProducts listProducts(ProductRepository productRepository) {
        return new ListProducts(productRepository);
    }

    @Bean
    CreateSupplier createSupplier(SupplierRepository supplierRepository) {
        return new CreateSupplier(supplierRepository);
    }

    @Bean
    GetSupplier getSupplier(SupplierRepository supplierRepository) {
        return new GetSupplier(supplierRepository);
    }

    @Bean
    ListSuppliers listSuppliers(SupplierRepository supplierRepository) {
        return new ListSuppliers(supplierRepository);
    }

    @Bean
    ReceiveGoods receiveGoods(SupplierRepository supplierRepository, ProductRepository productRepository,
                              GoodsReceiptRepository goodsReceiptRepository) {
        return new ReceiveGoods(supplierRepository, productRepository, goodsReceiptRepository);
    }

    @Bean
    GetGoodsReceipt getGoodsReceipt(GoodsReceiptRepository goodsReceiptRepository) {
        return new GetGoodsReceipt(goodsReceiptRepository);
    }

    @Bean
    GetStock getStock(ProductRepository productRepository, BatchRepository batchRepository) {
        return new GetStock(productRepository, batchRepository);
    }
}
