package com.gastromind.infrastructure.web.receipt;

import com.gastromind.application.receipt.GetGoodsReceipt;
import com.gastromind.application.receipt.ListGoodsReceipts;
import com.gastromind.application.receipt.ReceiptFilter;
import com.gastromind.application.receipt.ReceiveGoods;
import com.gastromind.domain.entity.GoodsReceipt;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/goods-receipts")
public class GoodsReceiptController {

    private final ReceiveGoods receiveGoods;
    private final GetGoodsReceipt getGoodsReceipt;
    private final ListGoodsReceipts listGoodsReceipts;

    public GoodsReceiptController(ReceiveGoods receiveGoods, GetGoodsReceipt getGoodsReceipt,
                                  ListGoodsReceipts listGoodsReceipts) {
        this.receiveGoods = receiveGoods;
        this.getGoodsReceipt = getGoodsReceipt;
        this.listGoodsReceipts = listGoodsReceipts;
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

    @GetMapping
    public List<GoodsReceiptSummaryResponse> list(
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return listGoodsReceipts.execute(new ReceiptFilter(supplierId, from, to)).stream()
                .map(GoodsReceiptSummaryResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public GoodsReceiptResponse getById(@PathVariable UUID id) {
        return GoodsReceiptResponse.from(getGoodsReceipt.execute(id));
    }
}
