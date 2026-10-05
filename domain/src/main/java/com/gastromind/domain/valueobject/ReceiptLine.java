package com.gastromind.domain.valueobject;

import com.gastromind.domain.entity.Batch;

import java.time.LocalDate;
import java.util.UUID;

//Foto de lo que entró: aunque el lote se vaya gastando, el albarán sigue diciendo lo mismo
public record ReceiptLine(UUID batchId, UUID productId, Quantity quantity, Money amount, LocalDate expirationDate,
                          String lotCode) {

    public static ReceiptLine of(Batch batch) {
        return new ReceiptLine(
                batch.getId(),
                batch.getProductId(),
                batch.getInitialQuantity(),
                batch.getPurchasePrice(),
                batch.getExpirationDate(),
                batch.getSku()
        );
    }
}
