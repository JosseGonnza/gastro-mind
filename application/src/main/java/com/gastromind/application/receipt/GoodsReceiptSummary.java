package com.gastromind.application.receipt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record GoodsReceiptSummary(UUID id, UUID supplierId, String supplierName, String deliveryNoteNumber,
                                  LocalDate receivedOn, int lineCount, BigDecimal total) {
}
