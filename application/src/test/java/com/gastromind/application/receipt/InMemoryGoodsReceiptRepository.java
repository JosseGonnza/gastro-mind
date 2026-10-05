package com.gastromind.application.receipt;

import com.gastromind.domain.entity.Batch;
import com.gastromind.domain.entity.GoodsReceipt;
import com.gastromind.domain.valueobject.ReceivedGoods;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryGoodsReceiptRepository implements GoodsReceiptRepository {

    private final Map<UUID, ReceivedGoods> receipts = new LinkedHashMap<>();
    private ReceiptFilter lastFilter;

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

    @Override
    public List<GoodsReceiptSummary> findSummaries(ReceiptFilter filter) {
        lastFilter = filter;
        return receipts.values().stream()
                .map(ReceivedGoods::receipt)
                .filter(receipt -> filter.supplierId() == null || receipt.getSupplierId().equals(filter.supplierId()))
                .filter(receipt -> filter.from() == null || !receipt.getReceivedOn().isBefore(filter.from()))
                .filter(receipt -> filter.to() == null || !receipt.getReceivedOn().isAfter(filter.to()))
                .sorted(Comparator.comparing(GoodsReceipt::getReceivedOn).reversed())
                .map(receipt -> new GoodsReceiptSummary(receipt.getId(), receipt.getSupplierId(), "",
                        receipt.getDeliveryNoteNumber(), receipt.getReceivedOn(), receipt.getLines().size(),
                        receipt.total().amount()))
                .toList();
    }

    public ReceiptFilter lastFilter() {
        return lastFilter;
    }

    public List<Batch> savedBatches() {
        return receipts.values().stream()
                .flatMap(received -> received.batches().stream())
                .toList();
    }
}
