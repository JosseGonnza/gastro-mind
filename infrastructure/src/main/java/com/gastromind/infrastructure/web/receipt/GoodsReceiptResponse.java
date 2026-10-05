package com.gastromind.infrastructure.web.receipt;

import com.gastromind.domain.entity.GoodsReceipt;
import com.gastromind.domain.valueobject.ReceiptLine;
import com.gastromind.domain.valueobject.UnitOfMeasure;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record GoodsReceiptResponse(UUID id, UUID supplierId, String deliveryNoteNumber, LocalDate receivedOn,
                                   BigDecimal total, List<Line> lines) {

    public record Line(UUID batchId, UUID productId, BigDecimal quantity, UnitOfMeasure unit, BigDecimal amount,
                       LocalDate expirationDate, String lotCode) {

        static Line from(ReceiptLine line) {
            return new Line(line.batchId(), line.productId(), line.quantity().amount(), line.quantity().unit(),
                    line.amount().amount(), line.expirationDate(), line.lotCode());
        }
    }

    public static GoodsReceiptResponse from(GoodsReceipt receipt) {
        return new GoodsReceiptResponse(
                receipt.getId(),
                receipt.getSupplierId(),
                receipt.getDeliveryNoteNumber(),
                receipt.getReceivedOn(),
                receipt.total().amount(),
                receipt.getLines().stream().map(Line::from).toList()
        );
    }
}
