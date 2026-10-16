package com.api.api.exception;

public class DestinationNotFoundException extends RuntimeException {

    public DestinationNotFoundException(Long id) {
        super("No existe un destino con id " + id);
    }
}
