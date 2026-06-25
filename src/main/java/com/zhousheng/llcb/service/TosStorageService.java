package com.zhousheng.llcb.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.volcengine.tos.TOSV2;
import com.volcengine.tos.TOSV2ClientBuilder;
import com.volcengine.tos.TosException;
import com.volcengine.tos.model.object.PutObjectBasicInput;
import com.volcengine.tos.model.object.PutObjectInput;
import com.zhousheng.llcb.common.BusinessException;
import com.zhousheng.llcb.entity.SysFile;
import com.zhousheng.llcb.mapper.SysFileMapper;
import com.zhousheng.llcb.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TosStorageService {

    private final TosProperties properties;
    private final UploadProperties uploadProperties;
    private final SysFileMapper fileMapper;

    public TosStorageService(TosProperties properties,
                             UploadProperties uploadProperties,
                             SysFileMapper fileMapper) {
        this.properties = properties;
        this.uploadProperties = uploadProperties;
        this.fileMapper = fileMapper;
    }

    @Transactional
    public SysFile upload(String bizType, Long bizId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        validateFile(file);
        Credentials credentials = readCredentials();
        String originalName = file.getOriginalFilename() == null ? "file" : FileUtil.getName(file.getOriginalFilename());
        String ext = FileUtil.extName(originalName).toLowerCase(Locale.ROOT);
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String objectKey = normalizePrefix(properties.prefix())
                + normalizeBizType(bizType)
                + "/"
                + date
                + "/"
                + IdUtil.getSnowflakeNextIdStr()
                + (StringUtils.hasText(ext) ? "." + ext : "");

        TOSV2 client = new TOSV2ClientBuilder()
                .build(properties.region(), properties.endpoint(), credentials.accessKey(), credentials.secretKey());
        try {
            PutObjectBasicInput basicInput = new PutObjectBasicInput()
                    .setBucket(properties.bucket())
                    .setKey(objectKey);
            PutObjectInput input = new PutObjectInput()
                    .setPutObjectBasicInput(basicInput)
                    .setContent(file.getInputStream());
            client.putObject(input);
        } catch (TosException | IOException ex) {
            throw new BusinessException("上传火山 TOS 失败: " + ex.getMessage());
        }

        SysFile record = new SysFile();
        record.setBizType(bizType);
        record.setBizId(bizId);
        record.setUploaderId(SecurityUtils.currentUserIdOrNull());
        record.setOriginalName(originalName);
        record.setFileName(Path.of(objectKey).getFileName().toString());
        record.setBucketName(properties.bucket());
        record.setObjectKey(objectKey);
        record.setFileUrl(trimTrailingSlash(properties.bucketDomain()) + "/" + objectKey);
        record.setContentType(file.getContentType());
        record.setFileExt(ext);
        record.setFileSize(file.getSize());
        record.setStatus("enabled");
        fileMapper.insert(record);
        return record;
    }

    private Credentials readCredentials() {
        if (!StringUtils.hasText(properties.accessKeyFile())) {
            throw new BusinessException("未配置 TOS AccessKey 文件路径");
        }
        Path path = Path.of(properties.accessKeyFile());
        if (!Files.exists(path)) {
            throw new BusinessException("TOS AccessKey 文件不存在: " + properties.accessKeyFile());
        }
        try {
            List<String> lines = Files.readAllLines(path).stream()
                    .map(String::trim)
                    .filter(line -> StringUtils.hasText(line) && !line.startsWith("#"))
                    .toList();
            String accessKey = null;
            String secretKey = null;
            List<String> looseValues = new ArrayList<>();
            for (String line : lines) {
                String[] pair = line.split("[:=]", 2);
                if (pair.length == 2) {
                    String key = pair[0].trim().toLowerCase(Locale.ROOT);
                    String value = pair[1].trim();
                    if (key.contains("access") && !key.contains("secret")) {
                        accessKey = value;
                    } else if (key.contains("secret")) {
                        secretKey = value;
                    }
                } else {
                    looseValues.add(line);
                }
            }
            if (!StringUtils.hasText(accessKey) && looseValues.size() >= 1) {
                accessKey = looseValues.get(0);
            }
            if (!StringUtils.hasText(secretKey) && looseValues.size() >= 2) {
                secretKey = looseValues.get(1);
            }
            if (!StringUtils.hasText(accessKey) || !StringUtils.hasText(secretKey)) {
                throw new BusinessException("TOS AccessKey 文件需包含 AccessKey 和 SecretKey");
            }
            return new Credentials(accessKey, secretKey);
        } catch (IOException ex) {
            throw new BusinessException("读取 TOS AccessKey 文件失败: " + ex.getMessage());
        }
    }

    private String normalizePrefix(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return "";
        }
        return prefix.endsWith("/") ? prefix : prefix + "/";
    }

    private String normalizeBizType(String bizType) {
        return StringUtils.hasText(bizType) ? bizType.replaceAll("[^a-zA-Z0-9_-]", "_") : "common";
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private void validateFile(MultipartFile file) {
        long maxBytes = uploadProperties.maxBytes() > 0 ? uploadProperties.maxBytes() : 20L * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BusinessException("上传文件超过大小限制");
        }
        String originalName = file.getOriginalFilename() == null ? "" : FileUtil.getName(file.getOriginalFilename());
        String extension = FileUtil.extName(originalName).toLowerCase(Locale.ROOT);
        Set<String> allowedExtensions = normalize(uploadProperties.allowedExtensions());
        if (!allowedExtensions.isEmpty() && !allowedExtensions.contains(extension)) {
            throw new BusinessException("不支持的文件扩展名");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        Set<String> allowedContentTypes = normalize(uploadProperties.allowedContentTypes());
        if (!allowedContentTypes.isEmpty()
                && !"application/octet-stream".equals(contentType)
                && !allowedContentTypes.contains(contentType)) {
            throw new BusinessException("不支持的文件类型");
        }
    }

    private Set<String> normalize(List<String> values) {
        if (values == null) {
            return Set.of();
        }
        return values.stream()
                .filter(StringUtils::hasText)
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    private record Credentials(String accessKey, String secretKey) {
    }
}
