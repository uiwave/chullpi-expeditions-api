package com.api.api.exception;

public class SlugAlreadyExistsException extends RuntimeException {

    public SlugAlreadyExistsException(String slug, String entity) {
        super("Ya existe " + entity + " con el slug '" + slug + "'");
    }
}
