# eureka

1. 建module

cloud-eureka-server7001

2. 改pom

server端依赖对比：

```xml
<dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
        <dependency>
            <groupId>com.xiaotu.cloud</groupId>
            <artifactId>cloud-api-common</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
```



3. 写yml
在resources目录下新建application.yml文件

```yml
server:
  port: 7001
eureka:
  instance:
    hostname: localhost
  client:
    # false表示不会向注册中心注册自己
    register-with-eureka: false
    # false表示自己端就是注册中心 职责就是维护服务实例，并不需要去检索服务
    fetch-registry: false
    service-url:
      # 设置与eurekaServer交互的地址查询服务和注册服务都需要依赖这个地址
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
```



4.主类

```java
@EnableEurekaServer
@SpringBootApplication
public class EurekaMain7001 {
    public static void main(String[] args) {
        SpringApplication.run(EurekaMain7001.class,args);
    }
}
```





启动失败了。。。  注意版本号呀！！！

![image-20221115174805383](images/readme/image-20221115174805383.png)

Boot版本恢复成2.1.x后  clean后 成功运行

![image-20221115175328908](images/readme/image-20221115175328908.png)





1. 引入依赖

   ```xml
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
       <version>2.1.3.RELEASE</version>
   </dependency>
   ```

2. yml添加配置

   ```yml
   eureka:
     client:
       register-with-eureka: true
       fetch-registry: true
       service-url:
         defaultZone: http://localhost:7001/eureka
   ```

3. 主配置类上增加 `@EnableEurekaClient`注解，表示这个项目是eureka的客户端

4. 启动项目，然后刷新页面，成功注册成注册中心



![image-20221115195808524](images/readme/image-20221115195808524.png)



 将EurekaClient端80注册进EurekaServer成为服务消费者consumer

1. 引入依赖

   ```xml
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
       <version>2.1.3.RELEASE</version>
   </dependency>
   ```

2. yml添加

   ```yml
   spring:
     application:
     	name: cloud-order-service
   eureka:
     client:
       register-with-eureka: true
       fetch-registry: true
       service-url:
         defaultZone: http://localhost:7001/eureka
   ```

   



![image-20221115204619598](images/readme/image-20221115204619598.png)





修改host

```
127.0.0.1 eureka7001.com
127.0.0.1 eureka7002.com
127.0.0.1 eureka7003.com
```



照着7001创建相同的两个Modules,7002,7003

修改7001的yml文件

```yml
server:
  port: 7001
eureka:
  instance:
    hostname: eureka7001.com #eureka服务器的实例名称
  client:
    # false表示不会向注册中心注册自己
    register-with-eureka: false
    # false表示自己端就是注册中心 职责就是维护服务实例，并不需要去检索服务
    fetch-registry: false
    service-url:
      # 单机
      # 设置与eurekaServer交互的地址查询服务和注册服务都需要依赖这个地址
      #defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
      defaultZone: http://eureka7002.com:7002/eureka/,http://eureka7003.com:7003/eureka/

      #defaultZone是固定写法，如果要自定义，需要按以下写法才对
#      region: eureka-server
#      availability-zone:
#        eureka-server: server1,server2
#      server-url:
#        server1: http://eureka7002.com:7002/eureka/
#        server2: http://eureka7003.com:7003/eureka/

```

同理 修改7002.7003的yml，然后启用所用项目 http://eureka7003.com:7003/

![image-20221115211800935](images/readme/image-20221115211800935.png)







1. 按照8001新建8002（只多建了一个提供者，建多了怕电脑受不了）。（除了要yml文件中需要改端口号和主配置类，其他直接复制8001的，yml文件中的应用名不需要改，因为是集群，所以应用名需要一致）

2. 分别在所有的提供者的PaymentController中加入：（这个@Value是spring的注解）

   ```java
   @Value("${server.port}")
   private String serverPort;
   
   ...
   return new CommonResult(200,"插入数据库成功,serverPort:"+serverPort,result);
   ...
   return new CommonResult(200,"查询成功,serverPort:"+serverPort,payment);
   
   ```

   

3. 修改消费者的OrderController，把写死的url改为服务名称：

   ```java
    private static final String PAYMENT_URL= "http://CLOUD-PAYMENT-SERVICE"; //注意名字需要为服务名！！！
   ```

   

4. 然后在消费者的ApplicationContextConfig里的restTemplate方法上加上`@LoadBalanced`，开启负载均衡功能。
5. 启动eurekaServer集群，启动提供者集群，启动消费者。
   如果启动提供者后出现，这个错误：Public Key Retrieval is not allowed
   请在yml文件中的datasource.datasource.url后加上&allowPublicKeyRetrieval=true即可解决。

多请求几次

![image-20221115213927810](images/readme/image-20221115213927810.png)



![image-20221115214008487](images/readme/image-20221115214008487.png)

