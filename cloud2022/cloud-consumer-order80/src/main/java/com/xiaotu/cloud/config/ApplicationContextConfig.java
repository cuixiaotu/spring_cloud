package com.xiaotu.cloud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationContextConfig {

    //从容器中添加一个RestTemplate
    //RestTemplate提供多种便捷方式访问远程http的方法
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

}
