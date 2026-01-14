package com.gastromind.domain.entity;

import com.gastromind.domain.valueobject.Money;
import com.gastromind.domain.valueobject.Quantity;

import java.time.LocalDate;
import java.util.UUID;

public class Batch {

    private  final UUID id;
    private final Product product;
    private final String sku;
    private final LocalDate entryDate;
    private final LocalDate expirationDate;
    private final Money purchasePrice;
    private final Quantity initialQuantity;
    private  Quantity currentQuantity;

    private Batch(UUID id, Product product, String sku, LocalDate entryDate, LocalDate expirationDate, Money purchasePrice, Quantity initialQuantity) {
        this.id = id;
        this.product = product;
        this.sku = sku;
        this.entryDate = entryDate;
        this.expirationDate = expirationDate;
        this.purchasePrice = purchasePrice;
        this.initialQuantity = initialQuantity;
        this.currentQuantity = initialQuantity;
    }

    public static Batch create(Product product, String sku, LocalDate expirationDate, Money purchasePrice, Quantity initialQuantity) {
        return new Batch(
                UUID.randomUUID(),
                product,
                sku,
                LocalDate.now(),
                expirationDate,
                purchasePrice,
                initialQuantity
        );
    }

    public UUID getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public String getSku() {
        return sku;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public Money getPurchasePrice() {
        return purchasePrice;
    }

    public Quantity getInitialQuantity() {
        return initialQuantity;
    }

    public Quantity getCurrentQuantity() {
        return currentQuantity;
    }
}
