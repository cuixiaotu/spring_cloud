package com.xiaotu.cloud.service;

import com.xiaotu.cloud.util.IdGeneratorSnowflake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class OrderService {

    @Autowired
    private IdGeneratorSnowflake idGenerator;

    public String getIDBySnowFlake(){
        //新建线程池 （5个线程）
        ExecutorService threadPool = Executors.newFixedThreadPool(5);
        for (int i = 0;i <= 20; i++){
            threadPool.submit(()->{
                System.out.println(idGenerator.snowflakeId());
            });
        }
        threadPool.shutdown();
        return "hello snowflake";
    }
}
