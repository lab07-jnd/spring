package com.project.ponto.exception;

public record FieldErrorDetail(
        String field,
        String message
) {}
