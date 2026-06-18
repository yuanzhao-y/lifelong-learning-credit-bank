package com.zhousheng.llcb.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llcb.jwt")
public record JwtProperties(String issuer, String secret, long expireMinutes) {
}
