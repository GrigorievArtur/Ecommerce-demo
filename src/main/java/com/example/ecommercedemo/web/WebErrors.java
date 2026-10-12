package com.example.ecommercedemo.web;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Flattens a {@link BindingResult} into a field→message map that FreeMarker templates can
 * read as {@code fieldErrors.email} etc.
 */
public final class WebErrors {

    private WebErrors() {
    }

    public static Map<String, String> fieldErrors(BindingResult binding) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : binding.getFieldErrors()) {
            errors.putIfAbsent(error.getField(),
                    error.getDefaultMessage() != null ? error.getDefaultMessage() : error.getCode());
        }
        return errors;
    }
}
