package com.xiaotu.cloud.dao;

import com.xiaotu.cloud.entities.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PaymentDao {

    //增
    int create(Payment payment);

    //改
    Payment getPaymentById(@Param("id") Long id);
}
