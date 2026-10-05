package com.gastromind.domain.valueobject;

import java.math.BigDecimal;
import java.util.UUID;

public record CostSheetLine(UUID productId, String productName, Quantity quantity, BigDecimal yieldPercentage,
                            Quantity grossQuantity, Money cost) {
}
