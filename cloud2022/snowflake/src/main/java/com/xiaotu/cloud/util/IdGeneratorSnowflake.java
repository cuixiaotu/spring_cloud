package com.xiaotu.cloud.util;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.net.NetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class IdGeneratorSnowflake {

    private long workerId = 0;//几号机房
    private long datacenterId = 1;//几号机器
    private Snowflake snowflake = new Snowflake(workerId,datacenterId);

    @PostConstruct //构造后开始执行 记载初始化工作
    public void init(){
        try {
            //获取本机IP地址
            workerId = NetUtil.ipv4ToLong(NetUtil.getLocalhostStr());
            log.info("当前机器的workerId " + workerId);
        }catch (Exception e){
            e.printStackTrace();
            log.warn("当前机器的workerId获取失败"+e);
            workerId = NetUtil.getLocalhostStr().hashCode();
        }
    }

    public synchronized long snowflakeId(){
        return snowflake.nextId();
    }

    public synchronized long snowflakeId(long workerId,long datacenterId){
        Snowflake snowflake = new Snowflake(workerId,datacenterId);
        return snowflake.nextId();
    }

    //测试
    public static void main(String[] args) {
        System.out.println(new IdGeneratorSnowflake().snowflakeId());
    }

}
