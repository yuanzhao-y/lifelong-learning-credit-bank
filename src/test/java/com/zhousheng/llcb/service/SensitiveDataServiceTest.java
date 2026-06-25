package com.zhousheng.llcb.service;

import com.zhousheng.llcb.security.CryptoProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SensitiveDataServiceTest {

    private final SensitiveDataService service = new SensitiveDataService(
            new CryptoProperties("unit-test-data-crypto-secret"));

    @Test
    void encryptsAndDecryptsValue() {
        String encrypted = service.encrypt("13800138000");

        assertThat(encrypted).isNotBlank();
        assertThat(encrypted).isNotEqualTo("13800138000");
        assertThat(service.decrypt(encrypted)).isEqualTo("13800138000");
    }

    @Test
    void hashesTrimmedValueConsistently() {
        assertThat(service.hash(" 13800138000 ")).isEqualTo(service.hash("13800138000"));
        assertThat(service.hash("13800138000")).isNotEqualTo(service.hash("13800138001"));
    }

    @Test
    void masksPhoneAndIdCard() {
        assertThat(service.maskPhone("13800138000")).isEqualTo("138****8000");
        assertThat(service.maskIdCard("110101199001011234")).isEqualTo("1101**********1234");
    }
}
