package com.zhousheng.llcb.common;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.validation.ConstraintViolationException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void preservesNotFoundHttpStatus() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleBusiness(new BusinessException(404, "missing"));

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().getCode()).isEqualTo(404);
    }

    @Test
    void preservesRateLimitHttpStatus() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleBusiness(new BusinessException(429, "limited"));

        assertThat(response.getStatusCode().value()).isEqualTo(429);
    }

    @Test
    void mapsNonHttpBusinessCodeToBadRequest() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleBusiness(new BusinessException(1001, "business"));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getCode()).isEqualTo(1001);
    }

    @Test
    void formatsFieldValidationErrors() {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "request");
        binding.addError(new FieldError("request", "username", "must not be blank"));
        binding.addError(new FieldError("request", "password", "must be strong"));

        ApiResponse<Void> response = handler.handleValidation(new MethodArgumentNotValidException(null, binding));

        assertThat(response.getCode()).isEqualTo(400);
        assertThat(response.getMessage())
                .contains("username: must not be blank")
                .contains("password: must be strong");
    }

    @Test
    void mapsConstraintViolationToBadRequest() {
        ApiResponse<Void> response = handler.handleConstraint(new ConstraintViolationException("page must be positive", null));

        assertThat(response.getCode()).isEqualTo(400);
        assertThat(response.getMessage()).contains("page must be positive");
    }

    @Test
    void mapsAccessDeniedToForbidden() {
        ApiResponse<Void> response = handler.handleDenied(new AccessDeniedException("denied"));

        assertThat(response.getCode()).isEqualTo(403);
    }

    @Test
    void mapsUnexpectedExceptionToInternalServerError() {
        ApiResponse<Void> response = handler.handleException(new IllegalStateException("boom"));

        assertThat(response.getCode()).isEqualTo(500);
    }
}
