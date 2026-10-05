package com.gastromind.application.receipt;

import com.gastromind.application.product.ProductNotFoundException;
import com.gastromind.application.product.ProductRepository;
import com.gastromind.application.supplier.SupplierNotFoundException;
import com.gastromind.application.supplier.SupplierRepository;
import com.gastromind.domain.entity.GoodsReceipt;
import com.gastromind.domain.entity.Product;
import com.gastromind.domain.entity.Supplier;
import com.gastromind.domain.valueobject.Money;
import com.gastromind.domain.valueobject.NewReceiptLine;
import com.gastromind.domain.valueobject.Quantity;
import com.gastromind.domain.valueobject.ReceivedGoods;

import java.util.List;

public class ReceiveGoods {

    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;

    public ReceiveGoods(SupplierRepository supplierRepository, ProductRepository productRepository,
                        GoodsReceiptRepository goodsReceiptRepository) {
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.goodsReceiptRepository = goodsReceiptRepository;
    }

    public GoodsReceipt execute(ReceiveGoodsCommand command) {
        Supplier supplier = supplierRepository.findById(command.supplierId())
                .orElseThrow(() -> new SupplierNotFoundException(command.supplierId()));
        List<NewReceiptLine> lines = command.lines() == null ? List.of() : command.lines().stream()
                .map(this::toNewLine)
                .toList();

        ReceivedGoods received = GoodsReceipt.register(supplier, command.deliveryNoteNumber(), lines);

        String deliveryNoteNumber = received.receipt().getDeliveryNoteNumber();
        if (goodsReceiptRepository.existsBySupplierAndDeliveryNoteNumber(supplier.getId(), deliveryNoteNumber)) {
            throw new DuplicateDeliveryNoteException(deliveryNoteNumber);
        }
        goodsReceiptRepository.save(received);
        return received.receipt();
    }

    private NewReceiptLine toNewLine(ReceiveGoodsCommand.Line line) {
        Product product = productRepository.findById(line.productId())
                .orElseThrow(() -> new ProductNotFoundException(line.productId()));
        Money amount = line.amount() == null ? null : Money.of(line.amount());
        return new NewReceiptLine(product, Quantity.of(line.quantity(), line.unit()), amount,
                line.expirationDate(), line.lotCode());
    }
}
