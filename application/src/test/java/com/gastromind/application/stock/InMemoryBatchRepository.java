package com.gastromind.application.stock;

import com.gastromind.domain.entity.Batch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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
}
