package com.zhousheng.llcb.common;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

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
}
