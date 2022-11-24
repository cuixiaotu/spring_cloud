# SpringCloud Config分布式配置中心

github创建配置项目：https://github.com/cuixiaotu/cloud-config



1. 新建模块cloud-config-center-3344

2. pom

   ```xml
       <dependencies>
           <!--添加消息总线RabbitMQ的支持-->
   <!--        <dependency>
               <groupId>org.springframework.cloud</groupId>
               <artifactId>spring-cloud-starter-bus-amqp</artifactId>
           </dependency>-->
           <!--config server-->
           <dependency>
               <groupId>org.springframework.cloud</groupId>
               <artifactId>spring-cloud-config-server</artifactId>
           </dependency>
           <!--eureka client(通过微服务名实现动态路由)-->
           <dependency>
               <groupId>org.springframework.cloud</groupId>
               <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
           </dependency>
           <dependency>
               <groupId>org.springframework.boot</groupId>
               <artifactId>spring-boot-starter-web</artifactId>
           </dependency>
           <dependency>
               <groupId>org.springframework.boot</groupId>
               <artifactId>spring-boot-starter-actuator</artifactId>
           </dependency>
           <!--热部署-->
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
           <dependency>
               <groupId>cn.hutool</groupId>
               <artifactId>hutool-all</artifactId>
               <version>5.8.10</version>
               <scope>test</scope>
           </dependency>
       </dependencies>
   ```

3. yml

   ```yml
   server:
     port: 3344
   spring:
     application:
       name: cloud-config-center
     cloud:
       config:
         server:
           git:
             uri: https://github.com/cuixiaotu/cloud-config.git
             search-paths: #搜索目录
               - cloud-config
             username: 616364596@qq.com
             password: XXXXXXXXXXXXXXXXX
         label: main #读取分组
   eureka:
     client:
       service-url:
         defaultZone: http://localhost:7001/eureka
   debug: true
   ```

   

4. 主启动类

   ```java
   @EnableConfigServer
   @SpringBootApplication
   public class ConfigCenterMain3344 {
       public static void main(String[] args) {
           SpringApplication.run(ConfigCenterMain3344.class,args);
       }
   }
   ```

5. 修改host

   ```tex
   127.0.0.1 config-3344.com
   ```

6. 启动7001，3344，请求：http://config-3344.com:3344/main/config-dev.yml

![image-20221123194607817](images/readme/image-20221123194607817.png)

读取规则

1.http://config-3344.com:3344/master/config-test.yml

2.http://config-3344.com:3344/config-test.yml

3.http://config-3344.com:3344/config/test/master



## Config客户端配置与测试

1. 新建模块cloud-config-client-3355

2. pom

   ```xml
   <dependencies>
           <dependency>
               <groupId>org.springframework.cloud</groupId>
               <artifactId>spring-cloud-starter-config</artifactId>
           </dependency>
           <dependency>
               <groupId>org.springframework.cloud</groupId>
               <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
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
       </dependencies>
   ```

3. bootstrap.yml

   ```yml
   server:
     port: 3355
   
   spring:
     application:
       name: config-client
     cloud:
       config:
         label: main
         name: config
         profile: dev
         uri: http://localhost:3344
   
   eureka:
     client:
       service-url:
         defaultZone: http://localhost:7001/eureka
   
   management:
     endpoint:
       web:
         exposure:
           include: "*"
   
   ```

4. 主配置类

   ```java
   @EnableEurekaClient
   @SpringBootApplication
   public class ConfigClientMain3355 {
       public static void main(String[] args) {
           SpringApplication.run(ConfigClientMain3355.class, args);
       }
   }
   ```

5. controller

   ```java
   @RefreshScope
   @RestController
   public class ConfigClientController {
   
       @Value("${config.info}")
       private String configInfo;
   
       @GetMapping("/configInfo")
       public String getConfigInfo() {
           return configInfo;
       }
   
   }
   ```

   