package com.gastromind.application.receipt;

import com.gastromind.domain.entity.GoodsReceipt;

import java.util.UUID;

public class GetGoodsReceipt {

    private final GoodsReceiptRepository goodsReceiptRepository;

    public GetGoodsReceipt(GoodsReceiptRepository goodsReceiptRepository) {
        this.goodsReceiptRepository = goodsReceiptRepository;
    }

    public GoodsReceipt execute(UUID id) {
        return goodsReceiptRepository.findById(id)
                .orElseThrow(() -> new GoodsReceiptNotFoundException(id));
    }
}
