package com.zhousheng.llcb.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llcb.tos")
public record TosProperties(
        String region,
        String endpoint,
        String bucket,
        String bucketDomain,
        String prefix,
        String accessKeyFile) {
}
