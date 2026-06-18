package com.zhousheng.llcb.service;

import cn.hutool.core.util.IdUtil;

public final class BizNoGenerator {

    private BizNoGenerator() {
    }

    public static String next(String prefix) {
        return prefix + IdUtil.getSnowflakeNextIdStr();
    }
}
