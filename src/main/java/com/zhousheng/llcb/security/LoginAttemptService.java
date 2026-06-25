package com.zhousheng.llcb.security;

import com.zhousheng.llcb.common.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class LoginAttemptService {

    private static final int MAX_FAILURES = 5;
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(15);

    private final StringRedisTemplate redisTemplate;

    public LoginAttemptService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void checkAllowed(String username, String ip) {
        if (failures(username, ip) >= MAX_FAILURES) {
            throw new BusinessException(429, "登录失败次数过多，请 15 分钟后重试");
        }
    }

    public void recordFailure(String username, String ip) {
        try {
            String key = key(username, ip);
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redisTemplate.expire(key, BLOCK_DURATION);
            }
        } catch (RuntimeException ex) {
            log.warn("Unable to record login failure in Redis", ex);
        }
    }

    public void clear(String username, String ip) {
        try {
            redisTemplate.delete(key(username, ip));
        } catch (RuntimeException ex) {
            log.warn("Unable to clear login failures in Redis", ex);
        }
    }

    private int failures(String username, String ip) {
        try {
            String value = redisTemplate.opsForValue().get(key(username, ip));
            return value == null ? 0 : Integer.parseInt(value);
        } catch (RuntimeException ex) {
            log.warn("Unable to read login failures from Redis", ex);
            return 0;
        }
    }

    private String key(String username, String ip) {
        String safeUsername = username == null ? "unknown" : username.trim().toLowerCase();
        String safeIp = ip == null ? "unknown" : ip;
        return "llcb:security:login-fail:" + safeUsername + ":" + safeIp;
    }
}
