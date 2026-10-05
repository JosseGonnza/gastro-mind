package com.gastromind.infrastructure.web.receipt;

import com.gastromind.application.receipt.GetGoodsReceipt;
import com.gastromind.application.receipt.ReceiveGoods;
import com.gastromind.domain.entity.GoodsReceipt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/goods-receipts")
public class GoodsReceiptController {

    private final ReceiveGoods receiveGoods;
    private final GetGoodsReceipt getGoodsReceipt;

    public GoodsReceiptController(ReceiveGoods receiveGoods, GetGoodsReceipt getGoodsReceipt) {
        this.receiveGoods = receiveGoods;
        this.getGoodsReceipt = getGoodsReceipt;
    }

    @PostMapping
    public ResponseEntity<GoodsReceiptResponse> register(@RequestBody ReceiveGoodsRequest request) {
        GoodsReceipt receipt = receiveGoods.execute(request.toCommand());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(receipt.getId())
                .toUri();
        return ResponseEntity.created(location).body(GoodsReceiptResponse.from(receipt));
    }

    @GetMapping("/{id}")
    public GoodsReceiptResponse getById(@PathVariable UUID id) {
        return GoodsReceiptResponse.from(getGoodsReceipt.execute(id));
    }
}
