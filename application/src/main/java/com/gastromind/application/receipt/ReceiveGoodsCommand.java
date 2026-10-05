package com.gastromind.application.receipt;

import com.gastromind.domain.valueobject.UnitOfMeasure;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ReceiveGoodsCommand(UUID supplierId, String deliveryNoteNumber, List<Line> lines) {

    public record Line(UUID productId, BigDecimal quantity, UnitOfMeasure unit, BigDecimal amount,
                       LocalDate expirationDate, String lotCode) {
    }
}
