package com.zhousheng.llcb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llcb.seed")
public record SeedProperties(String adminUsername, String adminPassword) {
}
