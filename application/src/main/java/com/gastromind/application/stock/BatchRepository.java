package com.gastromind.application.stock;

import com.gastromind.domain.entity.Batch;

import java.util.List;

public interface BatchRepository {

    List<Batch> findAvailable();
}
