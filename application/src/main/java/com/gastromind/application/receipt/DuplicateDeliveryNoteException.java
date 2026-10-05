package com.gastromind.application.receipt;

public class DuplicateDeliveryNoteException extends RuntimeException {

    public DuplicateDeliveryNoteException(String deliveryNoteNumber) {
        super("Supplier already has a delivery note " + deliveryNoteNumber);
    }
}
