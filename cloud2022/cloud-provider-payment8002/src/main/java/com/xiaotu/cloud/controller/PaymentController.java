package com.xiaotu.cloud.controller;


import com.xiaotu.cloud.entities.CommonResult;
import com.xiaotu.cloud.entities.Payment;
import com.xiaotu.cloud.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

//import javax.annotation.Resource;

@Slf4j
@RestController
public class PaymentController {

    @Value("${server.port}")
    private String serverPort;

    @Autowired
    private PaymentService paymentService;

    //Springframework的DiscoveryClient
    @Resource
    private DiscoveryClient discoveryClient;

    @GetMapping("/hello")
    public String hello() {
        return "hello world";
    }

    @GetMapping("/payment/lb")
    private String getPaymentLB(){
        return serverPort;
    }

    @GetMapping("/payment/discovery")
    public Object discovery(){
        //获取服务列表信息
        List<String> services = discoveryClient.getServices();
        for (String element:services){
            log.info("********element:"+element);
        }

        //获取CLOUD-PAYMENT-SERVICE服务的具体实例
        List<ServiceInstance> instances = discoveryClient.getInstances("CLOUD-PAYMENT-SERVICE");
        for (ServiceInstance instance : instances
        ) {
            log.info(instance.getInstanceId()+"\t"+instance.getHost()+"\t"+instance.getPort()+"\t"+instance.getClass());
        }

        return discoveryClient;
    }

    @PostMapping("/payment/create")
    public CommonResult create(@RequestBody Payment payment){
        int result = paymentService.create(payment);
        log.info("Payment +++++++" + payment);
        log.info("result  +++++++" + result);

        if (result >0){
            return new CommonResult(200,"插入数据库成功,serverPort:"+serverPort,result);
        }else {
            return new CommonResult(444,"插入数据库失败",result);
        }
    }

    @GetMapping("/payment/get/{id}")
    public CommonResult getPaymentById(@PathVariable("id") Long id){
        System.out.println("id  :"+id);
        Payment payment = paymentService.getPaymentById(id);
        log.info("Payment" + payment);

        if (payment != null){
            return new CommonResult(200,"查询成功,serverPort:"+serverPort,payment);
        }else {
            return new CommonResult(444,"没有对应记录，查询ID"+id);
        }
    }


}
