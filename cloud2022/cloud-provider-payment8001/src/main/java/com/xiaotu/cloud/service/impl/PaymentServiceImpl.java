package com.xiaotu.cloud.service.impl;

import com.xiaotu.cloud.dao.PaymentDao;
import com.xiaotu.cloud.entities.Payment;
import com.xiaotu.cloud.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class PaymentServiceImpl implements PaymentService {

    //@Resource
    @Autowired
    public PaymentDao paymentDao;


    public int create(Payment payment) {
        return paymentDao.create(payment);
    }

    public Payment getPaymentById(Long id) {
        return paymentDao.getPaymentById(id);
    }
}
