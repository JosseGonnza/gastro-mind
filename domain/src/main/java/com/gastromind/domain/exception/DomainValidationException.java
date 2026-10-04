package com.gastromind.domain.exception;

//Hereda de IllegalArgumentException: sigue siendo un dato no válido, pero la API puede distinguirlo de un fallo de programación
public class DomainValidationException extends IllegalArgumentException {

    public DomainValidationException(String message) {
        super(message);
    }
}
