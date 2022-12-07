package com.xiaotu.cloud.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.xiaotu.cloud.service.TestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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


    @GetMapping("/testHotKey")
    @SentinelResource(value = "testHotKey",blockHandler = "deal_testHotKey")
    public String testHotKey(@RequestParam(value="p1",required = false) String p1,
                             @RequestParam(value="p2",required = false) String p2){
        return "hostkey";
    }

    //兜底方法
    public String deal_testHotKey(String p1, String p2, BlockException exception){
        return "--------deal_testHotKey------";
    }
}
