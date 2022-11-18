package com.xiaotu.cloud.lb;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MyLB implements ILoadBalancer{

    //新建一个原子整型类
    private AtomicInteger atomicInteger = new AtomicInteger(0);

    public final int getAndIncrement(){
        int current;
        int next;
        do{
            current = atomicInteger.get();
            next = current >= Integer.MAX_VALUE ? 0 : current+1;
        }while (!this.atomicInteger.compareAndSet(current, next));
        System.out.println("*****第几次访问，次数next:"+next);
        return next;
    }

    @Override
    public ServiceInstance instance(List<ServiceInstance> serviceInstances) {
        if (serviceInstances.size() <= 0){
            return null;
        }
        //去余
        int index = getAndIncrement() % serviceInstances.size();
        //返回选择中的实例
        return serviceInstances.get(index);
    }
}
