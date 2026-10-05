package com.gastromind.application.receipt;

import java.time.LocalDate;
import java.util.UUID;

//Cualquiera de los tres puede ir vacío: sin filtro por ese campo
public record ReceiptFilter(UUID supplierId, LocalDate from, LocalDate to) {

    public static ReceiptFilter all() {
        return new ReceiptFilter(null, null, null);
    }
}
