package com.gastromind.domain.entity;

import com.gastromind.domain.exception.DomainValidationException;
import com.gastromind.domain.valueobject.Money;
import com.gastromind.domain.valueobject.NewReceiptLine;
import com.gastromind.domain.valueobject.ReceiptLine;
import com.gastromind.domain.valueobject.ReceivedGoods;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GoodsReceipt {

    private final UUID id;
    private final UUID supplierId;
    private final String deliveryNoteNumber;
    private final LocalDate receivedOn;
    private final List<ReceiptLine> lines;

    private GoodsReceipt(UUID id, UUID supplierId, String deliveryNoteNumber, LocalDate receivedOn, List<ReceiptLine> lines) {
        if (id == null) throw new DomainValidationException("Goods receipt id cannot be null");
        if (supplierId == null) throw new DomainValidationException("Supplier cannot be null");
        if (deliveryNoteNumber == null || deliveryNoteNumber.isBlank()) {
            throw new DomainValidationException("Delivery note number cannot be empty");
        }
        if (receivedOn == null) throw new DomainValidationException("Reception date cannot be null");
        if (lines == null || lines.isEmpty()) throw new DomainValidationException("A goods receipt needs at least one line");
        this.id = id;
        this.supplierId = supplierId;
        this.deliveryNoteNumber = deliveryNoteNumber.trim();
        this.receivedOn = receivedOn;
        this.lines = List.copyOf(lines);
    }

    //Se registra cuando llega el género: cada línea se convierte en un lote nuevo
    public static ReceivedGoods register(Supplier supplier, String deliveryNoteNumber, List<NewReceiptLine> newLines) {
        if (supplier == null) throw new DomainValidationException("Supplier cannot be null");
        if (deliveryNoteNumber == null || deliveryNoteNumber.isBlank()) {
            throw new DomainValidationException("Delivery note number cannot be empty");
        }
        if (newLines == null || newLines.isEmpty()) {
            throw new DomainValidationException("A goods receipt needs at least one line");
        }
        String number = deliveryNoteNumber.trim();
        List<Batch> batches = new ArrayList<>();
        for (int i = 0; i < newLines.size(); i++) {
            NewReceiptLine line = newLines.get(i);
            if (line == null) throw new DomainValidationException("Receipt line cannot be null");
            batches.add(Batch.create(line.product(), lotCodeFor(line, number, i + 1), line.expirationDate(),
                    line.amount(), line.quantity()));
        }
        GoodsReceipt receipt = new GoodsReceipt(UUID.randomUUID(), supplier.getId(), number, LocalDate.now(),
                toLines(batches));
        return new ReceivedGoods(receipt, List.copyOf(batches));
    }

    public static GoodsReceipt restore(UUID id, UUID supplierId, String deliveryNoteNumber, LocalDate receivedOn,
                                       List<Batch> batches) {
        if (batches == null) throw new DomainValidationException("A goods receipt needs at least one line");
        return new GoodsReceipt(id, supplierId, deliveryNoteNumber, receivedOn, toLines(batches));
    }

    public Money total() {
        return lines.stream()
                .map(ReceiptLine::amount)
                .reduce(Money.of(0.0), Money::add);
    }

    private static String lotCodeFor(NewReceiptLine line, String deliveryNoteNumber, int lineNumber) {
        if (line.lotCode() == null || line.lotCode().isBlank()) {
            return deliveryNoteNumber + "-" + lineNumber;
        }
        return line.lotCode().trim();
    }

    private static List<ReceiptLine> toLines(List<Batch> batches) {
        return batches.stream().map(ReceiptLine::of).toList();
    }

    public UUID getId() {
        return id;
    }

    public UUID getSupplierId() {
        return supplierId;
    }

    public String getDeliveryNoteNumber() {
        return deliveryNoteNumber;
    }

    public LocalDate getReceivedOn() {
        return receivedOn;
    }

    public List<ReceiptLine> getLines() {
        return lines;
    }
}
