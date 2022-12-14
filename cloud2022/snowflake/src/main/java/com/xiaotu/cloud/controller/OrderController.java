package com.xiaotu.cloud.controller;

import com.xiaotu.cloud.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/snowflake")
    public String index(){
        return orderService.getIDBySnowFlake();
    }
}
