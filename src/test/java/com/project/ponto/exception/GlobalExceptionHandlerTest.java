package com.project.ponto.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Must handle ResourceNotFoundException returning 404 and enriched ErrorResponse")
    void handleNotFound() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/funcionarios/99");
        ResourceNotFoundException ex = new ResourceNotFoundException("Employee not found with id: 99");

        ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("Not Found");
        assertThat(response.getBody().message()).isEqualTo("Employee not found with id: 99");
        assertThat(response.getBody().path()).isEqualTo("/funcionarios/99");
        assertThat(response.getBody().timestamp()).isNotNull();
        assertThat(response.getBody().fieldErrors()).isNull();
    }

    @Test
    @DisplayName("Must handle ConflictException returning 409 and enriched ErrorResponse")
    void handleConflict() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/funcionarios");
        ConflictException ex = new ConflictException("CPF already registered");

        ResponseEntity<ErrorResponse> response = handler.handleConflict(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().error()).isEqualTo("Conflict");
        assertThat(response.getBody().message()).isEqualTo("CPF already registered");
        assertThat(response.getBody().path()).isEqualTo("/funcionarios");
    }

    @Test
    @DisplayName("Must handle AccessDeniedException returning 403 Forbidden")
    void handleAccessDenied() {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/funcionarios/1");
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(403);
        assertThat(response.getBody().error()).isEqualTo("Forbidden");
        assertThat(response.getBody().message()).isEqualTo("Access Denied: You do not have permission to access this resource.");
        assertThat(response.getBody().path()).isEqualTo("/funcionarios/1");
    }

    @Test
    @DisplayName("Must handle HttpMessageNotReadableException returning 400 Bad Request")
    void handleHttpMessageNotReadable() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/funcionarios");
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);

        ResponseEntity<ErrorResponse> response = handler.handleHttpMessageNotReadable(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().message()).isEqualTo("Request body missing or malformed.");
        assertThat(response.getBody().path()).isEqualTo("/funcionarios");
    }

    @Test
    @DisplayName("Must handle IllegalArgumentException returning 400 and enriched ErrorResponse")
    void handleBadRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/funcionarios?page=0");
        IllegalArgumentException ex = new IllegalArgumentException("Page must be greater than or equal to 1");

        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().message()).isEqualTo("Page must be greater than or equal to 1");
        assertThat(response.getBody().path()).isEqualTo("/funcionarios?page=0");
    }

    @Test
    @DisplayName("Must handle MethodArgumentTypeMismatchException returning 400 with parameter name")
    void handleTypeMismatch() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/funcionarios?page=abc");
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("page");

        ResponseEntity<ErrorResponse> response = handler.handleTypeMismatch(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().message()).isEqualTo("Valor inválido para o parâmetro 'page'");
        assertThat(response.getBody().path()).isEqualTo("/funcionarios?page=abc");
    }

    @Test
    @DisplayName("Must handle MethodArgumentNotValidException with field errors list")
    void handleMethodArgumentNotValid() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/funcionarios");
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("dto", "email", "invalid email");
        FieldError fieldError2 = new FieldError("dto", "name", "must not be blank");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValid(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().fieldErrors()).hasSize(2);
        assertThat(response.getBody().fieldErrors().get(0).field()).isEqualTo("email");
        assertThat(response.getBody().fieldErrors().get(0).message()).isEqualTo("invalid email");
        assertThat(response.getBody().fieldErrors().get(1).field()).isEqualTo("name");
        assertThat(response.getBody().fieldErrors().get(1).message()).isEqualTo("must not be blank");
        assertThat(response.getBody().path()).isEqualTo("/funcionarios");
    }

    @Test
    @DisplayName("Must handle generic Exception returning 500 without leaking details")
    void handleGeneralException() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/funcionarios");
        RuntimeException ex = new RuntimeException("Sensitive database connection error");

        ResponseEntity<ErrorResponse> response = handler.handleGeneralException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().error()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred on the server.");
        assertThat(response.getBody().path()).isEqualTo("/funcionarios");
    }
}
