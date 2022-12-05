package com.xiaotu.cloud.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import org.springframework.stereotype.Service;

@Service
public class TestService {
    @SentinelResource("getTest")
    public void getTest(){
        System.out.println("getTest()");
    }
}
