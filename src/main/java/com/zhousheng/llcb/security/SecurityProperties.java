package com.zhousheng.llcb.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "llcb.security")
public record SecurityProperties(List<String> publicPaths) {
}
