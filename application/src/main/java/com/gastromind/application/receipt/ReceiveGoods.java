package com.gastromind.application.receipt;

import com.gastromind.application.product.ProductNotFoundException;
import com.gastromind.application.product.ProductRepository;
import com.gastromind.application.supplier.SupplierNotFoundException;
import com.gastromind.application.supplier.SupplierRepository;
import com.gastromind.domain.entity.GoodsReceipt;
import com.gastromind.domain.entity.Product;
import com.gastromind.domain.entity.Supplier;
import com.gastromind.domain.exception.DomainValidationException;
import com.gastromind.domain.valueobject.Money;
import com.gastromind.domain.valueobject.NewReceiptLine;
import com.gastromind.domain.valueobject.Quantity;
import com.gastromind.domain.valueobject.ReceivedGoods;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        if (command.supplierId() == null) throw new DomainValidationException("Supplier cannot be null");
        Supplier supplier = supplierRepository.findById(command.supplierId())
                .orElseThrow(() -> new SupplierNotFoundException(command.supplierId()));
        List<ReceiveGoodsCommand.Line> commandLines = command.lines() == null ? List.of() : command.lines();
        Map<UUID, Product> products = findProducts(commandLines);
        List<NewReceiptLine> lines = commandLines.stream()
                .map(line -> toNewLine(line, products))
                .toList();

        ReceivedGoods received = GoodsReceipt.register(supplier, command.deliveryNoteNumber(), lines);

        String deliveryNoteNumber = received.receipt().getDeliveryNoteNumber();
        if (goodsReceiptRepository.existsBySupplierAndDeliveryNoteNumber(supplier.getId(), deliveryNoteNumber)) {
            throw new DuplicateDeliveryNoteException(deliveryNoteNumber);
        }
        goodsReceiptRepository.save(received);
        return received.receipt();
    }

    //Todos los productos del albarán en una sola consulta, no una por línea
    private Map<UUID, Product> findProducts(List<ReceiveGoodsCommand.Line> lines) {
        for (ReceiveGoodsCommand.Line line : lines) {
            if (line == null) throw new DomainValidationException("Receipt line cannot be null");
            if (line.productId() == null) throw new DomainValidationException("Product cannot be null");
        }
        Set<UUID> productIds = lines.stream()
                .map(ReceiveGoodsCommand.Line::productId)
                .collect(Collectors.toSet());
        return productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
    }

    private NewReceiptLine toNewLine(ReceiveGoodsCommand.Line line, Map<UUID, Product> products) {
        Product product = products.get(line.productId());
        if (product == null) throw new ProductNotFoundException(line.productId());
        Money amount = line.amount() == null ? null : Money.of(line.amount());
        return new NewReceiptLine(product, Quantity.of(line.quantity(), line.unit()), amount,
                line.expirationDate(), line.lotCode());
    }
}
