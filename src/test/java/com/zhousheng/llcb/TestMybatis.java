package com.zhousheng.llcb;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;

public final class TestMybatis {

    private TestMybatis() {
    }

    public static void initialize(Class<?>... entityTypes) {
        MybatisConfiguration configuration = new MybatisConfiguration();
        for (Class<?> entityType : entityTypes) {
            TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, entityType.getName()), entityType);
        }
    }
}
