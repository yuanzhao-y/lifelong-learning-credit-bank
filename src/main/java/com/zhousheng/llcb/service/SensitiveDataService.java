package com.zhousheng.llcb.service;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.zhousheng.llcb.security.CryptoProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Service
public class SensitiveDataService {

    private final AES aes;

    public SensitiveDataService(CryptoProperties properties) {
        byte[] key = Arrays.copyOf(SecureUtil.sha256(properties.secret()).getBytes(StandardCharsets.UTF_8), 16);
        this.aes = SecureUtil.aes(key);
    }

    public String encrypt(String value) {
        return StringUtils.hasText(value) ? aes.encryptBase64(value) : null;
    }

    public String decrypt(String value) {
        return StringUtils.hasText(value) ? aes.decryptStr(value) : null;
    }

    public String hash(String value) {
        return StringUtils.hasText(value) ? SecureUtil.sha256(value.trim()) : null;
    }

    public String maskPhone(String phone) {
        if (!StringUtils.hasText(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    public String maskIdCard(String idCard) {
        if (!StringUtils.hasText(idCard) || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 4) + "**********" + idCard.substring(idCard.length() - 4);
    }
}
