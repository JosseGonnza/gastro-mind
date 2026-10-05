package com.gastromind.application.receipt;

import com.gastromind.domain.entity.GoodsReceipt;
import com.gastromind.domain.valueobject.ReceivedGoods;

import java.util.Optional;
import java.util.UUID;

public interface GoodsReceiptRepository {

    void save(ReceivedGoods receivedGoods);

    Optional<GoodsReceipt> findById(UUID id);

    boolean existsBySupplierAndDeliveryNoteNumber(UUID supplierId, String deliveryNoteNumber);
}
