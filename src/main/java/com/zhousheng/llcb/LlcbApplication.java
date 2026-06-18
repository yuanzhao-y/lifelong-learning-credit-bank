package com.zhousheng.llcb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@MapperScan("com.zhousheng.llcb.mapper")
@ConfigurationPropertiesScan
public class LlcbApplication {

    public static void main(String[] args) {
        SpringApplication.run(LlcbApplication.class, args);
    }
}
