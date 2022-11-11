package com.xiaotu.controller;


import com.xiaotu.entities.CommonResult;
import com.xiaotu.entities.Payment;
import com.xiaotu.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sun.util.resources.cldr.es.CalendarData_es_PY;

import javax.annotation.Resource;

@RestController
@Slf4j
public class PaymentController {

    @Resource
    private PaymentService paymentService;

    @PostMapping("/payment/create")
    public CommonResult create(@RequestBody Payment payment){
        int result = paymentService.create(payment);
        log.info("Payment +++++++" + payment);
        log.info("result  +++++++" + result);

        if (result >0){
            return new CommonResult(200,"插入数据库成功",result);
        }else {
            return new CommonResult(444,"插入数据库失败",result);
        }
    }

    public CommonResult getPaymentById(@Param("id") Long id){
        Payment payment = paymentService.getPaymentById(id);
        log.info("Payment" + payment);

        if (payment != null){
            return new CommonResult(200,"查询成功",payment);
        }else {
            return new CommonResult(444,"没有对应记录，查询ID"+id);
        }
    }


}
