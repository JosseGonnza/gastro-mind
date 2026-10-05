package com.gastromind.application.stock;

import com.gastromind.domain.entity.Batch;
import com.gastromind.domain.valueobject.PurchasePrice;

import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InMemoryBatchRepository implements BatchRepository {

    private final List<Batch> batches = new ArrayList<>();

    public void add(Batch... newBatches) {
        batches.addAll(Arrays.asList(newBatches));
    }

    @Override
    public List<Batch> findAvailable() {
        return batches.stream()
                .filter(batch -> !batch.getCurrentQuantity().isZero())
                .sorted(Comparator.comparing(Batch::getExpirationDate))
                .toList();
    }

    @Override
    public Map<UUID, PurchasePrice> findLastPurchases(Collection<UUID> productIds) {
        Comparator<Batch> latestFirst = Comparator.comparing(Batch::getEntryDate)
                .thenComparing(batch -> batch.getPurchasePrice().amount().divide(batch.getInitialQuantity().amount(),
                        MathContext.DECIMAL64))
                .reversed();
        Map<UUID, PurchasePrice> lastPurchases = new HashMap<>();
        batches.stream()
                .filter(batch -> productIds.contains(batch.getProductId()))
                .sorted(latestFirst)
                .forEach(batch -> lastPurchases.putIfAbsent(batch.getProductId(),
                        new PurchasePrice(batch.getPurchasePrice(), batch.getInitialQuantity())));
        return lastPurchases;
    }
}
