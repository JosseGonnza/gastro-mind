package com.gastromind.domain.valueobject;

import com.gastromind.domain.entity.Product;

import java.time.LocalDate;

//Lo que se escribe al registrar el albarán; las reglas las aplica el lote que crea
public record NewReceiptLine(Product product, Quantity quantity, Money amount, LocalDate expirationDate, String lotCode) {
}
