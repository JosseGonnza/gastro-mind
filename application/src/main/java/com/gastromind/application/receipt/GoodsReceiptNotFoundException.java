package com.gastromind.application.receipt;

import java.util.UUID;

public class GoodsReceiptNotFoundException extends RuntimeException {

    public GoodsReceiptNotFoundException(UUID goodsReceiptId) {
        super("Goods receipt not found: " + goodsReceiptId);
    }
}
