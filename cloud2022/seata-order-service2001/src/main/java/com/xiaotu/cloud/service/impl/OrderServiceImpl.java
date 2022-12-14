package com.xiaotu.cloud.service.impl;

import com.xiaotu.cloud.dao.OrderDao;
import com.xiaotu.cloud.domain.Order;
import com.xiaotu.cloud.service.AccountService;
import com.xiaotu.cloud.service.OrderService;
import com.xiaotu.cloud.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderDao orderDao;
    @Resource
    private StorageService storageService;
    @Resource
    private AccountService accountService;

    @Override
    public void create(Order order) {
        //1.创建订单
        log.info("------>开始创建订单");
        orderDao.create(order);

        //2.扣减库存
        log.info("------>订单微服务开始调用库存，做扣减count");
        storageService.decrease(order.getProductId(),order.getCount());
        log.info("------>订单微服务开始调用库存，扣减完成");

        //3.扣减账号余额
        log.info("------>订单微服务开始调用账号，做扣减money");
        accountService.decrease(order.getUserId(),order.getMoney());
        log.info("------>订单微服务开始调用账号，扣减完成");

        //3.扣减账号余额
        log.info("------>修改订单状态");
        orderDao.update(order.getUserId(),1);
        log.info("------>修改订单状态完成");

        log.info("------->新建订单完成");
    }
}
