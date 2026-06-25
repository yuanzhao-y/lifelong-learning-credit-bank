package com.zhousheng.llcb.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "llcb.upload")
public record UploadProperties(long maxBytes,
                               List<String> allowedExtensions,
                               List<String> allowedContentTypes) {
}
