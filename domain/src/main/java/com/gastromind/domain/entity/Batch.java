package com.gastromind.domain.entity;

import com.gastromind.domain.valueobject.Money;
import com.gastromind.domain.valueobject.Quantity;
import com.gastromind.domain.valueobject.UnitOfMeasure;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

public class Batch {

    private  final UUID id;
    private final UUID productId;
    private final UnitOfMeasure unit;
    private final String sku;
    private final LocalDate entryDate;
    private final LocalDate expirationDate;
    private final Money purchasePrice;
    private final Quantity initialQuantity;
    private  Quantity currentQuantity;

    private Batch(UUID id, UUID productId, UnitOfMeasure unit, String sku, LocalDate entryDate, LocalDate expirationDate,
                  Money purchasePrice, Quantity initialQuantity, Quantity currentQuantity) {
        validateInvariants(id, productId, unit, sku, entryDate, expirationDate, purchasePrice, initialQuantity, currentQuantity);
        this.id = id;
        this.productId = productId;
        this.unit = unit;
        this.sku = sku;
        this.entryDate = entryDate;
        this.expirationDate = expirationDate;
        this.purchasePrice = purchasePrice;
        this.initialQuantity = initialQuantity.to(unit);
        this.currentQuantity = currentQuantity.to(unit);
    }

    //Alta de género: aplica las reglas de entrada (no se recibe nada caducado)
    public static Batch create(Product product, String sku, LocalDate expirationDate, Money purchasePrice, Quantity initialQuantity) {
        if (product == null) throw new IllegalArgumentException("Product cannot be null");
        if (expirationDate != null && expirationDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot accept expired products");
        }
        return new Batch(
                UUID.randomUUID(),
                product.getId(),
                product.getUnit(),
                sku,
                LocalDate.now(),
                expirationDate,
                purchasePrice,
                initialQuantity,
                initialQuantity
        );
    }

    //Reconstruye un lote ya guardado tal como estaba: puede haber caducado o estar gastado a medias
    public static Batch restore(UUID id, UUID productId, UnitOfMeasure unit, String sku, LocalDate entryDate,
                                LocalDate expirationDate, Money purchasePrice, Quantity initialQuantity,
                                Quantity currentQuantity) {
        return new Batch(id, productId, unit, sku, entryDate, expirationDate, purchasePrice, initialQuantity, currentQuantity);
    }

    public Money getUnitCost() {
        BigDecimal totalCost = purchasePrice.amount();
        BigDecimal originalQuantity = initialQuantity.amount();
        BigDecimal unitCost = totalCost.divide(originalQuantity, 2, RoundingMode.HALF_EVEN);
        return new Money(unitCost, purchasePrice.currency());
    }

    public void consume(Quantity amountToConsume) {
        if (amountToConsume.isZero()) throw new IllegalArgumentException("Quantity cannot be zero or less");
        this.currentQuantity = this.currentQuantity.subtract(amountToConsume);
    }

    private static void validateInvariants(UUID id, UUID productId, UnitOfMeasure unit, String sku, LocalDate entryDate,
                                           LocalDate expirationDate, Money purchasePrice, Quantity initialQuantity,
                                           Quantity currentQuantity) {
        if (id == null) throw new IllegalArgumentException("Batch ID cannot be null");
        if (productId == null) throw new IllegalArgumentException("Product cannot be null");
        if (unit == null) throw new IllegalArgumentException("Unit cannot be null");
        if (sku == null || sku.isBlank()) throw new IllegalArgumentException("SKU cannot be empty");
        if (purchasePrice == null) throw new IllegalArgumentException("Price cannot be null");
        if (initialQuantity == null) throw new IllegalArgumentException("Initial quantity cannot be null");
        if (currentQuantity == null) throw new IllegalArgumentException("Current quantity cannot be null");
        if (!initialQuantity.unit().isConvertibleTo(unit) || !currentQuantity.unit().isConvertibleTo(unit)) {
            throw new IllegalArgumentException("Quantity unit must be compatible with product unit");
        }
        if (!initialQuantity.hasEnough(currentQuantity)) {
            throw new IllegalArgumentException("Current quantity cannot exceed initial quantity");
        }
        if (entryDate == null) throw new IllegalArgumentException("Entry date cannot be null");
        if (expirationDate == null) throw new IllegalArgumentException("Expiration date cannot be null");
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public UnitOfMeasure getUnit() {
        return unit;
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
