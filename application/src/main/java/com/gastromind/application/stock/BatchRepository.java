package com.gastromind.application.stock;

import com.gastromind.domain.entity.Batch;
import com.gastromind.domain.valueobject.PurchasePrice;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface BatchRepository {

    List<Batch> findAvailable();

    //Los productos que no se han comprado nunca no aparecen en el mapa
    Map<UUID, PurchasePrice> findLastPurchases(Collection<UUID> productIds);
}
