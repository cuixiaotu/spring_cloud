package com.xiaotu.cloud.controller;

import com.xiaotu.cloud.service.TestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.sql.Time;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
public class FlowLimitController {

    @Resource
    TestService testService;

    @GetMapping("/testA")
    public String testA(){
        testService.getTest();
        return "testA";
    }

    @GetMapping("/testB")
    public String testB(){
        log.info(Thread.currentThread().getName()+"\t ....test B");
        testService.getTest();
        return "testB";
    }

    @GetMapping("/testD")
    public String testD(){
        try {
            TimeUnit.SECONDS.sleep(1);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        log.info("testD 测试RT");
        return "----testD";
    }
}
