package com.api.api.exception;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(Long id) {
        super("No existe una categoría con id " + id);
    }
}
