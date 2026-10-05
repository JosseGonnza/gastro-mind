package com.gastromind.application.receipt;

import com.gastromind.domain.exception.DomainValidationException;

import java.util.List;

public class ListGoodsReceipts {

    private final GoodsReceiptRepository goodsReceiptRepository;

    public ListGoodsReceipts(GoodsReceiptRepository goodsReceiptRepository) {
        this.goodsReceiptRepository = goodsReceiptRepository;
    }

    public List<GoodsReceiptSummary> execute(ReceiptFilter filter) {
        ReceiptFilter effective = filter == null ? ReceiptFilter.all() : filter;
        if (effective.from() != null && effective.to() != null && effective.from().isAfter(effective.to())) {
            throw new DomainValidationException("From date cannot be after to date");
        }
        return goodsReceiptRepository.findSummaries(effective);
    }
}
