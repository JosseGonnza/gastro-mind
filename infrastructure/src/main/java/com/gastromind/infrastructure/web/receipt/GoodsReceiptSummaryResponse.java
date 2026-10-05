package com.gastromind.infrastructure.web.receipt;

import com.gastromind.application.receipt.GoodsReceiptSummary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record GoodsReceiptSummaryResponse(UUID id, UUID supplierId, String supplierName, String deliveryNoteNumber,
                                          LocalDate receivedOn, int lineCount, BigDecimal total) {

    public static GoodsReceiptSummaryResponse from(GoodsReceiptSummary summary) {
        return new GoodsReceiptSummaryResponse(
                summary.id(),
                summary.supplierId(),
                summary.supplierName(),
                summary.deliveryNoteNumber(),
                summary.receivedOn(),
                summary.lineCount(),
                summary.total()
        );
    }
}
