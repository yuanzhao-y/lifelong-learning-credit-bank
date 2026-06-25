package com.zhousheng.llcb.security;

import com.zhousheng.llcb.common.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service = new LoginAttemptService(redisTemplate);
    }

    @Test
    void blocksAfterFiveFailures() {
        when(valueOperations.get(anyString())).thenReturn("5");

        assertThatThrownBy(() -> service.checkAllowed("QA_User", "127.0.0.1"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(429);
    }

    @Test
    void firstFailureCreatesExpiringCounter() {
        when(valueOperations.increment(anyString())).thenReturn(1L);

        service.recordFailure("QA_User", "127.0.0.1");

        verify(redisTemplate).expire(anyString(), any(Duration.class));
    }

    @Test
    void successfulLoginClearsCounter() {
        service.clear("QA_User", "127.0.0.1");

        verify(redisTemplate).delete("llcb:security:login-fail:qa_user:127.0.0.1");
    }

    @Test
    void redisFailureDoesNotTakeAuthenticationServiceDown() {
        when(valueOperations.get(anyString())).thenThrow(new IllegalStateException("redis unavailable"));

        assertThatCode(() -> service.checkAllowed("qa_user", "127.0.0.1")).doesNotThrowAnyException();
    }
}
