package com.xiaotu.cloud.service;

import cn.hutool.core.util.IdUtil;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.TimeUnit;

@Service
public class PaymentService {

    //正常访问方法
    public String paymentInfo_OK(Integer id){
        return "线程池：" + Thread.currentThread().getName() + "\t paymentInfo_OK， id ="+ id;
    }

    @HystrixCommand(fallbackMethod = "paymentInfo_TimeoutHandler",commandProperties = {
            //设置自身超时调用时间的峰值为3秒，峰值内可以正常运行，超过了需要有兜底逻辑，服务降级fallback
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "3000")
    })
    public String paymentInfo_Timeout(Integer id){
        //int error = 1/0;
        int timeNumber = 5;
        try {
            TimeUnit.SECONDS.sleep(timeNumber);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return  "线程池：" + Thread.currentThread().getName() + "\t paymentInfo_Timeout， id ="+ id + ", 耗时："+timeNumber+"秒";
    }

    public String paymentInfo_TimeoutHandler(Integer id){
        return  "8001提供者，线程池：" + Thread.currentThread().getName() +
                "\t paymentInfo_TimeoutHandler系统繁忙，请稍后再试 id ="+ id;
    }


    @HystrixCommand(
            fallbackMethod = "paymentCircuitBreaker_fallback",commandProperties = {
                    @HystrixProperty(name = "circuitBreaker.enabled",value="true"),//开启熔断器
                    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold",value="10"),//请求总数阈值
                    @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds",value="1000"),//休眠时间窗口期（休眠多久进入半开模式，单位毫秒，默认5秒）
                    @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage",value="60"),//请求错误率到达多少跳闸（百分比%，默认50%）
    })
    public String paymentCircuitBreaker(@PathVariable("id") Integer id){
        if (id < 0){
            throw new RuntimeException("*****id 不能为负数");
        }
        String serialNumber = IdUtil.simpleUUID();
        return Thread.currentThread().getName() + "\t" +
                "调用成功，流水号"+serialNumber;
    }

    public String paymentCircuitBreaker_fallback(@PathVariable("id") Integer id){
        return  "id 不能为负数，请稍后再试， id: "+id;
    }

}
