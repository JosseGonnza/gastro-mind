package com.gastromind.infrastructure.web.stock;

import com.gastromind.application.stock.GetStock;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stock")
public class StockController {

    private final GetStock getStock;

    public StockController(GetStock getStock) {
        this.getStock = getStock;
    }

    @GetMapping
    public List<ProductStockResponse> list() {
        return getStock.execute().stream()
                .map(ProductStockResponse::from)
                .toList();
    }
}
