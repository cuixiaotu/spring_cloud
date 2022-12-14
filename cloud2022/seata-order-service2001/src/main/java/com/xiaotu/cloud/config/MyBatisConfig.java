package com.xiaotu.cloud.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.stereotype.Component;

@Component
@MapperScan("com.xiaotu.cloud.dao")
public class MyBatisConfig {

}
