package com.api.api.exception;

public class SlugAlreadyExistsException extends RuntimeException {

    public SlugAlreadyExistsException(String slug) {
        super("Ya existe un destino con el slug '" + slug + "'");
    }
}
