# 十七、SpringCloud Alibaba入门简介

### Netflix进入维护模式

解决合理bug,其余不再更新

## Spring Alibaba简介

Spring官网：https://spring.io/projects/spring-cloud-alibaba
GitHub：https://github.com/alibaba/spring-cloud-alibaba
GitHub中文文档：https://github.com/alibaba/spring-cloud-alibaba/blob/master/README-zh.md
Spring Cloud Alibaba参考文档：https://spring-cloud-alibaba-group.github.io/github-pages/greenwich/spring-cloud-alibaba.html





# 十八、SpringCloud Alibaba [Nacos](https://so.csdn.net/so/search?q=Nacos&spm=1001.2101.3001.7020)服务注册和配置中心

Nacos

官网：[https://nacos.io/zh-cn/](https://nacos.io/zh-cn/)
GitHub：https://github.com/alibaba/Nacos

各注册中心比较

| 服务注册与发现框架 | CAP模型 | 控制台管理 | 社区活跃度 |
| ------------------ | ------- | ---------- | ---------- |
| Eureka             | AP      | 支持       | 低         |
| Zookeeper          | CP      | 不支持     | 中         |
| Consul             | CP      | 支持       | 高         |
| Nacos              | AP      | 支持       | 高         |

![nacos_map](images/SpringCloudAlibaba/nacosMap.jpg)

![nacos_landscape.png](images/SpringCloudAlibaba/1533045871534-e64b8031-008c-4dfc-b6e8-12a597a003fb.png)

## 安装并运行nacos

在docker上安装nacos

```sh
docker pull nacos/nacos-server
```

运行nacos,(启动报错了。。。关闭之前不用的docker，重启正常了)

```sh
docker run --env MODE=standalone --name nacos -d -p 8848:8848 nacos/nacos-server
```

浏览器输入：`http://10.0.41.31:8848/nacos/`。默认账号密码nacos

![image-20221201201456232](images/SpringCloudAlibaba/image-20221201201456232.png)



## Nacos作为服务注册中心演示

官方文档：https://spring-cloud-alibaba-group.github.io/github-pages/greenwich/spring-cloud-alibaba.html



1. 新建模块cloudalibaba-provider-payment9001

2. pom

   ```xml
   <dependencies>
       <dependency>
           <groupId>com.alibaba.cloud</groupId>
           <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
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
           <artifactId>spring-boot-starter-test</artifactId>
           <scope>test</scope>
       </dependency>
       <dependency>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-devtools</artifactId>
           <scope>runtime</scope>
           <optional>true</optional>
       </dependency>
   </dependencies>
   ```

3. yml

   ```yml
   server:
     port: 9001
   spring:
     application:
       name: nacos-payment-provider
     cloud:
       nacos:
         discovery:
           server-addr: 10.0.41.31:8848
   management:
     endpoints:
       web:
         exposure:
           include: '*'
   ```

4. 主启动类

   ```java
   @EnableDiscoveryClient
   @SpringBootApplication
   public class PaymentMain9001 {
       public static void main(String[] args) {
           SpringApplication.run(PaymentMain9001.class,args);
       }
   }
   ```

5. 新建controller.PaymentController

   ```java
   @EnableDiscoveryClient
   @SpringBootApplication
   public class PaymentMain9001 {
       public static void main(String[] args) {
           SpringApplication.run(PaymentMain9001.class,args);
       }
   }
   ```

6. 测试 启动9001

7. 参照9001新建9002，建立提供者集群启动

![image-20221201202041464](images/SpringCloudAlibaba/image-20221201202041464.png)



### 基于Nacos的服务消费者

1. 新建模块cloudalibaba-consumer-nacos-order83

2. pom（nacos集成了ribbon，实现负载均衡）

   ```xml
   <dependencies>
       <!-- spring cloud alibaba nacos -->
       <dependency>
           <groupId>com.alibaba.cloud</groupId>
           <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
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
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-test</artifactId>
           <scope>test</scope>
       </dependency>
   
   </dependencies>
   ```

3. yml

   ```yml
   server:
     port: 83
   spring:
     application:
       name: nacos-order-consumer
     cloud:
       nacos:
         discovery:
           server-addr: 10.0.41.31:8848
   
   #消费者要访问的微服务名称（成功注册nacos的服务提供者）
   service-url:
     nacos-user-service: http://nacos-payment-provider
   
   ```

4. 主启动类

   ```java
   @EnableDiscoveryClient
   @SpringBootApplication
   public class OrderNacosMain83 {
       public static void main(String[] args) {
           SpringApplication.run(OrderNacosMain83.class,args);
       }
   }
   ```

5. 新建config.ApplicationContextConfig

   ```java
   @Configuration
   public class ApplicationContextConfig {
   
       @Bean
       @LoadBalanced
       public RestTemplate getRestTemplate() {
           return new RestTemplate();
       }
   }
   ```

6. 新建controller.OrderNacosController

   ```java
   @Slf4j
   @RestController
   public class OrderNacosController {
       @Resource
       private RestTemplate restTemplate;
   
       @Value("${service-url.nacos-user-service}")
       private String serverURL;
   
   
       @GetMapping("/consumer/payment/nacos/{id}")
       public String paymentInfo(@PathVariable("id") Long id){
           return restTemplate.getForObject(serverURL + "/payment/nacos/" + id, String.class);
       }
       
   }
   ```

7. 测试 启动9001 9002 83

   ![image-20221202104359964](images/SpringCloudAlibaba/image-20221202104359964.png)

### 整合Feign







