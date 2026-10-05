package com.gastromind.domain.valueobject;

import com.gastromind.domain.entity.Batch;
import com.gastromind.domain.entity.GoodsReceipt;

import java.util.List;

public record ReceivedGoods(GoodsReceipt receipt, List<Batch> batches) {
}
