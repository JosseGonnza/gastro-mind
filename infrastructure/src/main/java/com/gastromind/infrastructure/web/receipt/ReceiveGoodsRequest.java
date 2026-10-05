package com.gastromind.infrastructure.web.receipt;

import com.gastromind.application.receipt.ReceiveGoodsCommand;
import com.gastromind.domain.valueobject.UnitOfMeasure;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ReceiveGoodsRequest(UUID supplierId, String deliveryNoteNumber, List<Line> lines) {

    public record Line(UUID productId, BigDecimal quantity, UnitOfMeasure unit, BigDecimal amount,
                       LocalDate expirationDate, String lotCode) {
    }

    public ReceiveGoodsCommand toCommand() {
        List<ReceiveGoodsCommand.Line> commandLines = lines == null ? null : lines.stream()
                .map(line -> new ReceiveGoodsCommand.Line(line.productId(), line.quantity(), line.unit(),
                        line.amount(), line.expirationDate(), line.lotCode()))
                .toList();
        return new ReceiveGoodsCommand(supplierId, deliveryNoteNumber, commandLines);
    }
}
