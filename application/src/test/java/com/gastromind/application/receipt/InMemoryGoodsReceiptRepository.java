package com.gastromind.application.receipt;

import com.gastromind.domain.entity.Batch;
import com.gastromind.domain.entity.GoodsReceipt;
import com.gastromind.domain.valueobject.ReceivedGoods;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryGoodsReceiptRepository implements GoodsReceiptRepository {

    private final Map<UUID, ReceivedGoods> receipts = new LinkedHashMap<>();

    @Override
    public void save(ReceivedGoods receivedGoods) {
        receipts.put(receivedGoods.receipt().getId(), receivedGoods);
    }

    @Override
    public Optional<GoodsReceipt> findById(UUID id) {
        return Optional.ofNullable(receipts.get(id)).map(ReceivedGoods::receipt);
    }

    @Override
    public boolean existsBySupplierAndDeliveryNoteNumber(UUID supplierId, String deliveryNoteNumber) {
        return receipts.values().stream()
                .map(ReceivedGoods::receipt)
                .anyMatch(receipt -> receipt.getSupplierId().equals(supplierId)
                        && receipt.getDeliveryNoteNumber().equals(deliveryNoteNumber));
    }

    public List<Batch> savedBatches() {
        return receipts.values().stream()
                .flatMap(received -> received.batches().stream())
                .toList();
    }
}
