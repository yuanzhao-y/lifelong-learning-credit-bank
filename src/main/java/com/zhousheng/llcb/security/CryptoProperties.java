package com.zhousheng.llcb.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llcb.crypto")
public record CryptoProperties(String secret) {
}
