package com.zhousheng.llcb.config;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MybatisPlusConfigTest {

    @Test
    void pageNormalizesUnsafeArguments() {
        Page<Object> page = MybatisPlusConfig.page(0, 999);

        assertThat(page.getCurrent()).isEqualTo(1);
        assertThat(page.getSize()).isEqualTo(100);
    }
}
