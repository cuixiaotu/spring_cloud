





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





