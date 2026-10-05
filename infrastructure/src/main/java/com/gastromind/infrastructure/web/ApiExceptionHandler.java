package com.gastromind.infrastructure.web;

import com.gastromind.application.product.ProductNotFoundException;
import com.gastromind.application.receipt.DuplicateDeliveryNoteException;
import com.gastromind.application.receipt.GoodsReceiptNotFoundException;
import com.gastromind.application.supplier.SupplierNotFoundException;
import com.gastromind.domain.exception.DomainValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    ProblemDetail handleProductNotFound(ProductNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(SupplierNotFoundException.class)
    ProblemDetail handleSupplierNotFound(SupplierNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(GoodsReceiptNotFoundException.class)
    ProblemDetail handleGoodsReceiptNotFound(GoodsReceiptNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(DuplicateDeliveryNoteException.class)
    ProblemDetail handleDuplicateDeliveryNote(DuplicateDeliveryNoteException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(DomainValidationException.class)
    ProblemDetail handleInvalidInput(DomainValidationException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }
}
