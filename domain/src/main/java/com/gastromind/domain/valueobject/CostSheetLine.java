package com.gastromind.domain.valueobject;

import java.math.BigDecimal;
import java.util.UUID;

//Sin compras del producto, lastPurchase y cost llegan a null
public record CostSheetLine(UUID productId, String productName, Quantity quantity, BigDecimal yieldPercentage,
                            Quantity grossQuantity, PurchasePrice lastPurchase, Money cost) {
}
