package com.gastromind.domain.exception;

import com.gastromind.domain.entity.Product;
import com.gastromind.domain.valueobject.Quantity;

//Extiende de RE, no obligas al cliente a capturarlas con try-catch
public class NotEnoughStockException extends RuntimeException{

    private final Product product;
    private final Quantity requested;
    private final Quantity available;

    public NotEnoughStockException(Product product, Quantity requested, Quantity available) {
        super(String.format(
                "Not enough stock for product '%s'. Requested: '%s', Available: '%s'",
                product.getName(),
                requested.value(),
                available.value()
        ));
        this.product = product;
        this.requested = requested;
        this.available = available;
    }

    public Product getProduct() {
        return product;
    }

    public Quantity getRequested() {
        return requested;
    }

    public Quantity getAvailable() {
        return available;
    }
}
