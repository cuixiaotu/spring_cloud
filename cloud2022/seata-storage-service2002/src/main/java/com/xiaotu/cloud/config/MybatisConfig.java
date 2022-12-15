package com.xiaotu.cloud.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.xiaotu.cloud.dao")
public class MybatisConfig {
}
