#  二十、SpringCloud Alibaba Seata 处理分布式事务



## 分布式事务问题



单体应用被拆分成微服务应用，原来的三个模块被拆分成二个独立的应用，分别使用三个独立的数据源，
业务操作需要调用三个服务来完成。此时每个服务内部的数据一致性由本地事务来保证，但是全局的数据一致性问题没法保证。

用户购买商品的业务逻辑。整个业务逻辑由3个微服务提供支持:
仓储服务: 对给定的商品扣除仓储数量
订单服务: 根据采购需求创建订单。
帐户服务: 从用户帐户中扣除余额

![img](images/Seata/solution.png)



## Seata简介

分布式事务解决方案，致力于在微服务架构下提供高性能和简单易用的分布式事务服务。

官网：http://seata.io/zh-cn/

## Seata术语

#### TC (Transaction Coordinator) - 事务协调者

维护全局和分支事务的状态，驱动全局事务提交或回滚。

#### TM (Transaction Manager) - 事务管理器

定义全局事务的范围：开始全局事务、提交或回滚全局事务。

#### RM (Resource Manager) - 资源管理器

管理分支事务处理的资源，与TC交谈以注册分支事务和报告分支事务的状态，并驱动分支事务提交或回滚。

1. TM 向 TC 申请开启一个全局事务，全局事务创建成功并生成一个全局唯一的 XID

2. XID 在微服务调用链路的上下文中传播

3. RM 向 TC 注册分支事务，将其纳入 XID 对应全局事务的管辖

4. TM 向 TC 发起针对 XID 的全局提交或回滚决议;

5. TC 调度 XID 下管辖的全部分支事务完成提交或回滚请求

  

## Seata-Server安装

下载：https://github.com/seata/seata/releases 

( 新版的用spring写的 配置走application.yml)

1.4.2和1.5.0后的配置有些不一样，这里按1.4.2稳定版配置.

1. 按前文已经配置好 nacos和mysql。

2. seata mysql相关数据库表

   ```sql
   -- the table to store GlobalSession data
   CREATE TABLE IF NOT EXISTS `global_table`
   (
       `xid`                       VARCHAR(128) NOT NULL,
       `transaction_id`            BIGINT,
       `status`                    TINYINT      NOT NULL,
       `application_id`            VARCHAR(32),
       `transaction_service_group` VARCHAR(32),
       `transaction_name`          VARCHAR(128),
       `timeout`                   INT,
       `begin_time`                BIGINT,
       `application_data`          VARCHAR(2000),
       `gmt_create`                DATETIME,
       `gmt_modified`              DATETIME,
       PRIMARY KEY (`xid`),
       KEY `idx_gmt_modified_status` (`gmt_modified`, `status`),
       KEY `idx_transaction_id` (`transaction_id`)
   ) ENGINE = InnoDB
     DEFAULT CHARSET = utf8;
   
   -- the table to store BranchSession data
   CREATE TABLE IF NOT EXISTS `branch_table`
   (
       `branch_id`         BIGINT       NOT NULL,
       `xid`               VARCHAR(128) NOT NULL,
       `transaction_id`    BIGINT,
       `resource_group_id` VARCHAR(32),
       `resource_id`       VARCHAR(256),
       `branch_type`       VARCHAR(8),
       `status`            TINYINT,
       `client_id`         VARCHAR(64),
       `application_data`  VARCHAR(2000),
       `gmt_create`        DATETIME(6),
       `gmt_modified`      DATETIME(6),
       PRIMARY KEY (`branch_id`),
       KEY `idx_xid` (`xid`)
   ) ENGINE = InnoDB
     DEFAULT CHARSET = utf8;
   
   -- the table to store lock data
   CREATE TABLE IF NOT EXISTS `lock_table`
   (
       `row_key`        VARCHAR(128) NOT NULL,
       `xid`            VARCHAR(128),
       `transaction_id` BIGINT,
       `branch_id`      BIGINT       NOT NULL,
       `resource_id`    VARCHAR(256),
       `table_name`     VARCHAR(32),
       `pk`             VARCHAR(36),
       `gmt_create`     DATETIME,
       `gmt_modified`   DATETIME,
       PRIMARY KEY (`row_key`),
       KEY `idx_branch_id` (`branch_id`)
   ) ENGINE = InnoDB
     DEFAULT CHARSET = utf8;
   ```

3. docker 启动seata

   ```sh
   docker run -d --name seata -p 8091:8091 -e SEATA_IP=你想指定的ip -e SEATA_PORT=8091 seataio/seata-server:1.4.2
   ```

4. 修改seata配置

5. 由于seata容器没有内置vim，我们可以直接将文件夹cp到宿主机外来编辑好了，再cp回去。

   ```sh
   docker cp 容器id:seata-server/resources 需要放置的目录
   ```

6. 使用如下代码获取两个容器的ip地址

   ```sh
   docker inspect --foramt='{{.NetworkSettings.IPAddress}}' ID/NAMES
   ```

7. Nacos-config.txt编辑如下内容

   ```
   ```

8. 修改registry.conf

   ```
   
   ```

9. 配置完成后使用如下命令复制到容器中，并重启

   ```sh
   docker cp /home/seata/resoures/registry.conf seata:seata-server/resources/
   docker restart seata
   docker logs -f seata
   ```

   

   

## 订单/库存/账户业务微服务准备

### 订单模块

1. 新建模块seata-order-service2001

2. pom

   ```xml
   <dependencies>
     <dependency>
       <groupId>com.alibaba.cloud</groupId>
       <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
     </dependency>
   
     <dependency>
       <groupId>com.alibaba.cloud</groupId>
       <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
       <!--            <exclusions>
                   <exclusion>
                       <groupId>io.seata</groupId>
                       <artifactId>seata-all</artifactId>
                   </exclusion>
               </exclusions>-->
     </dependency>
     <!--        <dependency>
               <groupId>io.seata</groupId>
               <artifactId>seata-all</artifactId>
               <version>1.2.0</version>
           </dependency>-->
     <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-openfeign</artifactId>
     </dependency>
     <dependency>
       <groupId>org.mybatis.spring.boot</groupId>
       <artifactId>mybatis-spring-boot-starter</artifactId>
     </dependency>
     <dependency>
       <groupId>com.alibaba</groupId>
       <artifactId>druid-spring-boot-starter</artifactId>
     </dependency>
     <dependency>
       <groupId>mysql</groupId>
       <artifactId>mysql-connector-java</artifactId>
     </dependency>
     <!--jdbc-->
     <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-jdbc</artifactId>
     </dependency>
     <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-actuator</artifactId>
     </dependency>
     <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-test</artifactId>
     </dependency>
   
   </dependencies>
   ```

3. yml

   ```yml
   server:
     port: 2001
   spring:
     application:
       name: seata-order-service
     cloud:
       alibaba:
         seata:
           # 自定义事务组名称需要与seata-server中对应
           tx-service-group: my_test_tx_service #因为seata的file.config文件没有service模块，事务组名默认为my_test_service
           #service要与tx-service-group对齐，vgroupMapping和grouplist再service的下一级
           service:
             vgroupMapping:
               #需要和tx-service-group一致
               my_test_service: default
             grouplist:
               # seata server的地址配置 此处集群配置是数组
               default: 10.0.41.31:8091
       nacos:
         discovery:
           server-addr: 10.0.41.31:8848 # nacos
     datasource:
       # 当前数据源操作类型
       type: com.alibaba.druid.pool.DruidDataSource
       # mysql驱动类
       driver-class-name: com.mysql.cj.jdbc.Driver
       url: jdbc:mysql://localhost:3306/seata_storage?useUnicode=true&charset=utf-8&useSSL=true&serverTimezone=UTC
       username: root
       password: 123456
   feign:
     hystrix:
       enabled: false
   logging:
     level:
       io:
         seata: info
   mybatis:
     mapper-locations: classpath*:mapper/*.xml
   ```

4. file.conf

   ```conf
   ```

5. registry.conf

   ```
   ```

6. domain.CommonResult

   ```java
   @Data
   @AllArgsConstructor
   @NoArgsConstructor
   public class CommonResult<T> {
       private Integer code;
       private String message;
       private T data;
   
       public CommonResult(Integer code, String message){
           this(code, message, null);
       }
   }
   ```

   domain.Order

   ```java
   @Data
   @AllArgsConstructor
   @NoArgsConstructor
   public class Order {
   
       private Long id;
   
       private Long userId;
   
       private Long productId;
   
       private Integer count;
   
       private BigDecimal money;
   
       private Integer status; //订单状态 0创建中 1已完结
   
   }
   ```

7. Dao.OrderDao

   ```java
   @Mapper
   public interface OrderDao {
   
       //1.新建订单
       int create(Order order);
   
       //2.修改订单状态，从0到1
       int update(@Param("userId") Long userId, @Param("status") Integer status);
   }
   ```

8. resource/mapper/OrderMapper.xml

   ```xml
   <?xml version="1.0" encoding="UTF-8" ?>
   <!DOCTYPE mapper
           PUBLIC "-//com.xiaotu.mybatis.org//DTD Mapper 3.0//EN"
           "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
   <mapper namespace="com.xiaotu.cloud.dao.AccountDao">
   
       <resultMap id="BaseResultMap" type="com.xiaotu.cloud.domain.Order">
           <id column="id" property="id" jdbcType="BIGINT"/>
           <result column="user_id" property="userId" jdbcType="BIGINT"/>
           <result column="product_id" property="productId" jdbcType="BIGINT" />
           <result column="count"  property="count" jdbcType="BIGINT"/>
           <result column="money" property="money" jdbcType="DECIMAL" />
           <result column="status" property="status" jdbcType="INTEGER"/>
       </resultMap>
   
       <insert id="create" parameterType="com.xiaotu.cloud.domain.Order"
               useGeneratedKeys="true" keyProperty="id">
           insert into t_order(`user_id`,`product_uid`,`count`,`money`,`status`)
               values (#{userId},#{productId},#{count},#{money},0);
       </insert>
   
       <update id="update" parameterType="com.xiaotu.cloud.domain.Order">
           update t_order set `status` = 1
               where `user_id` = #{userId} and `status` = #{status}
       </update>
   
   </mapper>
   ```

9. service.StorageService

   ```java
   @FeignClient("seata-storage-service")
   public interface StorageService {
   
       //减库存
       @PostMapping(value = "/storage/decrease")
       CommonResult decrease(@RequestParam("productId") Long productId, @RequestParam("count") Integer count);
   }
   ```

   service.AccountService

   ```java
   @FeignClient(value = "seata-account-service")
   public interface AccountService {
   
       @PostMapping(value = "/account/decrease")
       CommonResult decrease(@RequestParam("userId") Long userId, @RequestParam("money")BigDecimal money);
   }
   ```

   service.OrderService

   ```java
   public interface OrderService {
       void create(Order order);
   }
   ```

10. impl.OrerServiceImpl

    ```java
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
    
            log.info("------->新建订单完成");    }
    }
    
    ```

11. controller.OrderController

    ```java
    @RestController
    public class OrderController{
      @Resource
      private OrderService orderService;
      
      @GetMapping("/order/create")
      public CommonResult create(Order order){
      		orderService.create(order);
        	return new CommonResult(200,"订单创建成功！");
      }
    }
    ```

12. config.MybatisConfig

    ```java
    @MapperScan("com.xiaotu.cloud.dao")
    @Configutation
    public class MybatisConfig{
      
    }
    ```

    config.DataSourceProxyConfig

    ```java
    
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
    
    ```

13. 主启动类

    ```java
    @SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
    @EnableFeignClients
    @EnableDiscoveryClient
    public class SeataAccountMain2003 {
        public static void main(String[] args) {
            SpringApplication.run(SeataAccountMain2003.class,args);
        }
    }
    ```

14. 启动2001







### 库存模块

1. 新建模块seata-storage-service2002

2. pom

   ```xml
      <dependencies>
           <dependency>
               <groupId>com.alibaba.cloud</groupId>
               <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
           </dependency>
           <dependency>
               <groupId>com.alibaba.cloud</groupId>
               <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
               <exclusions>
                   <exclusion>
                       <groupId>io.seata</groupId>
                       <artifactId>seata-all</artifactId>
                   </exclusion>
               </exclusions>
           </dependency>
           <dependency>
               <groupId>io.seata</groupId>
               <artifactId>seata-all</artifactId>
           </dependency>
           <dependency>
               <groupId>org.springframework.cloud</groupId>
               <artifactId>spring-cloud-starter-openfeign</artifactId>
           </dependency>
           <dependency>
               <groupId>com.alibaba</groupId>
               <artifactId>druid-spring-boot-starter</artifactId>
           </dependency>
           <dependency>
               <groupId>org.mybatis.spring.boot</groupId>
               <artifactId>mybatis-spring-boot-starter</artifactId>
           </dependency>
           <dependency>
               <groupId>mysql</groupId>
               <artifactId>mysql-connector-java</artifactId>
           </dependency>
           <dependency>
               <groupId>org.springframework.boot</groupId>
               <artifactId>spring-boot-starter-jdbc</artifactId>
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

3. yml

   ```yml
   server:
     port: 2002
   spring:
     application:
       name: seata-storage-service
     cloud:
       alibaba:
         seata:
           # 自定义的事务组需要与seata-server对应
           tx-service-group: my_test_tx_group
           # service要与tx-service-group对齐，vgroupMapping和grouplist在service的下一级
           service:
             vgroupMapping:
               my_test_tx_group: default
             grouplist:
               # seata server的地址配置，此处集群配置的是个数组
               default: 10
       nacos:
         discovery:
           server-addr: 10.0.41.31:8848
   
     datasource:
       type: com.alibaba.druid.pool.DruidDataSource
       driver-class-name: com.mysql.cj.jdbc.Driver
       url: jdbc:mysql://localhost:3306/seata?useUnicode=true&charset=utf-8&useSSL=true&serverTimezone=UTC
       username: root
       password: 123456
   feign:
     hystrix:
       enabled: false
   logging:
     level:
       io:
         seata: info
   
   mybatis:
     mapper-locations: classpath*:mapper/*.xml
   ```

4. file.conf

   ```javascript
   transport {
   	  # tcp udt unix-domain-socket
   	  type = "TCP"
   	  #NIO NATIVE
   	  server = "NIO"
   	  #enable heartbeat
   	  heartbeat = true
   	  # the client batch send request enable
   	  enableClientBatchSendRequest = true
   	  #thread factory for netty
   	  threadFactory {
   	    bossThreadPrefix = "NettyBoss"
   	    workerThreadPrefix = "NettyServerNIOWorker"
   	    serverExecutorThread-prefix = "NettyServerBizHandler"
   	    shareBossWorker = false
   	    clientSelectorThreadPrefix = "NettyClientSelector"
   	    clientSelectorThreadSize = 1
   	    clientWorkerThreadPrefix = "NettyClientWorkerThread"
   	    # netty boss thread size,will not be used for UDT
   	    bossThreadSize = 1
   	    #auto default pin or 8
   	    workerThreadSize = "default"
   	  }
   	  shutdown {
   	    # when destroy server, wait seconds
   	    wait = 3
   	  }
   	  serialization = "seata"
   	  compressor = "none"
   	}
   	service {
   	  vgroupMapping.my_test_tx_group = "default"
   	  default.grouplist = "10.211.55.26:8091"
   	  enableDegrade = false
   	  disableGlobalTransaction = false
   	}
   
   	client {
   	  rm {
   	    asyncCommitBufferLimit = 10000
   	    lock {
   	      retryInterval = 10
   	      retryTimes = 30
   	      retryPolicyBranchRollbackOnConflict = true
   	    }
   	    reportRetryCount = 5
   	    tableMetaCheckEnable = false
   	    reportSuccessEnable = false
   	    sagaBranchRegisterEnable = false
   	  }
   	  tm {
   	    commitRetryCount = 5
   	    rollbackRetryCount = 5
   	    degradeCheck = false
   	    degradeCheckPeriod = 2000
   	    degradeCheckAllowTimes = 10
   	  }
   	  undo {
   	    dataValidation = true
   	    onlyCareUpdateColumns = true
   	    logSerialization = "jackson"
   	    logTable = "undo_log"
   	  }
   	  log {
   	    exceptionRate = 100
   	  }
   	}
   ```

5. registry.conf

   ```javascript
   registry {
   	  # file 、nacos 、eureka、redis、zk、consul、etcd3、sofa
   	  type = "nacos"
   
   	  nacos {
   	    application = "seata-server"
   	    serverAddr = "10.211.55.26:8848"    #nacos
   	    namespace = ""
   	    username = ""
   	    password = ""
   	  }
   	  eureka {
   	    serviceUrl = "http://localhost:8761/eureka"
   	    weight = "1"
   	  }
   	  redis {
   	    serverAddr = "localhost:6379"
   	    db = "0"
   	    password = ""
   	    timeout = "0"
   	  }
   	  zk {
   	    serverAddr = "127.0.0.1:2181"
   	    sessionTimeout = 6000
   	    connectTimeout = 2000
   	    username = ""
   	    password = ""
   	  }
   	  consul {
   	    serverAddr = "127.0.0.1:8500"
   	  }
   	  etcd3 {
   	    serverAddr = "http://localhost:2379"
   	  }
   	  sofa {
   	    serverAddr = "127.0.0.1:9603"
   	    region = "DEFAULT_ZONE"
   	    datacenter = "DefaultDataCenter"
   	    group = "SEATA_GROUP"
   	    addressWaitTime = "3000"
   	  }
   	  file {
   	    name = "file.conf"
   	  }
   	}
   
   	config {
   	  # file、nacos 、apollo、zk、consul、etcd3、springCloudConfig
   	  type = "file"
   
   	  nacos {
   	    serverAddr = "127.0.0.1:8848"
   	    namespace = ""
   	    group = "SEATA_GROUP"
   	    username = ""
   	    password = ""
   	  }
   	  consul {
   	    serverAddr = "127.0.0.1:8500"
   	  }
   	  apollo {
   	    appId = "seata-server"
   	    apolloMeta = "http://192.168.1.204:8801"
   	    namespace = "application"
   	  }
   	  zk {
   	    serverAddr = "127.0.0.1:2181"
   	    sessionTimeout = 6000
   	    connectTimeout = 2000
   	    username = ""
   	    password = ""
   	  }
   	  etcd3 {
   	    serverAddr = "http://localhost:2379"
   	  }
   	  file {
   	    name = "file.conf"
   	  }
   	}
   ```

6. domain.ComminResult

   ```java
   @Data
   @AllArgsConstructor
   @NoArgsConstructor
   public class CommonResult<T> {
       private Integer code;
       private String message;
       private T data;
   
       public CommonResult(Integer code,String message){
           this(code,message,null);
       }
   }
   ```

   domain.Storage

   ```java
   @AllArgsConstructor
   @NoArgsConstructor
   @Data
   public class Storage {
       private Long id;
       private Long productId;
       private Integer total;
       private Integer used;
       private Integer residue;
   }
   ```

7. dao.StorageDao

   ```java
   @Mapper
   public interface StorageDao {
       void decrease(@Param("productId") Long productId, @Param("count") Integer count);
   }
   ```

8. mapper/StorageMapper.xml

   ```xml
   <?xml version="1.0" encoding="UTF-8" ?>
   <!DOCTYPE mapper
           PUBLIC "-//com.xiaotu.mybatis.org//DTD Mapper 3.0//EN"
           "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
   <mapper namespace="com.xiaotu.cloud.dao.StorageDao">
       <resultMap id="BaseResultMap" type="com.xiaotu.cloud.domain.Storage">
           <id column="id" property="id" jdbcType="BIGINT"/>
           <result column="product_id" property="productId" jdbcType="BIGINT"/>
           <result column="total" property="total" jdbcType="INTEGER"/>
           <result column="used" property="used" jdbcType="INTEGER"/>
           <result column="residue" property="residue" jdbcType="INTEGER"/>
       </resultMap>
   
       <update id="decrease">
           update t_storage set used = used + #{count} , residue = residue - #{count}
           where product_id = #{productId};
       </update>
   
   </mapper>
   ```

9. service.StorageService

   ```java
   public interface StorageService {
       void decrease(Long productId, Integer count);
   }
   ```

10. service.impl.StorageServiceImpl

    ```java
    @Service
    public class StorageServiceImpl implements StorageService {
        private static final Logger LOGGER = LoggerFactory.getLogger(StorageServiceImpl.class);
    
        @Resource
        private StorageDao storageDao;
    
        @Override
        public void decrease(Long productId, Integer count) {
            LOGGER.info("----> StorageService中扣减库存 ");
            storageDao.decrease(productId, count);
            LOGGER.info("----> StorageService中扣减库存完成");
        }
    }
    ```

11. controller.StorageController

    ```java
    @RestController
    public class StorageController {
    
        @Resource
        private StorageService storageService;
    
        @RequestMapping("/storage/decrease")
        public CommonResult decrease(@RequestParam("productId") Long productId, @RequestParam("count") Integer count){
            storageService.decrease(productId, count);
            return new CommonResult(200,"扣减库存成功");
        }
    }
    ```

12. config.MyBatisConfig

    ```java
    @Configuration
    @MapperScan("com.xiaotu.cloud.dao")
    public class MybatisConfig {
    }
    ```

    config.DataSourceProxyConfig

    ```java
    @Configuration
    public class DataSourceProxyConfig {
        @Value("${mybatis.mapperLocations}")
        private String mapperLocations;
    
        @Bean
        @ConfigurationProperties(prefix = "spring.datasource")
        public DataSource druidDataSource(){
            return new DruidDataSource();
        }
    
        @Bean
        public DataSourceProxy dataSourceProxy(DataSource druidDataSource){
            return new DataSourceProxy(druidDataSource);
        }
    
        @Bean
        public SqlSessionFactory  sqlSessionFactoryBean(DataSourceProxy dataSourceProxy) throws Exception{
            SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
            bean.setDataSource(dataSourceProxy);
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            bean.setMapperLocations(resolver.getResources(mapperLocations));
            return bean.getObject();
        }
    ```

13. 主启动类

    ```java
    @SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
    @EnableFeignClients
    @EnableDiscoveryClient
    public class SeataStorageMain2002 {
        public static void main(String[] args) {
            SpringApplication.run(SeataStorageMain2002.class,args);
        }
    }
    ```

14. 启动2002





### 账户模块

1. 新建模块seata-account-service2003

2. pom

   ```xml
   <dependencies>
           <dependency>
               <groupId>com.alibaba.cloud</groupId>
               <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
           </dependency>
           <dependency>
               <groupId>com.alibaba.cloud</groupId>
               <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
               <exclusions>
                   <exclusion>
                       <groupId>io.seata</groupId>
                       <artifactId>seata-all</artifactId>
                   </exclusion>
               </exclusions>
           </dependency>
           <dependency>
               <groupId>io.seata</groupId>
               <artifactId>seata-all</artifactId>
           </dependency>
           <dependency>
               <groupId>org.springframework.cloud</groupId>
               <artifactId>spring-cloud-starter-openfeign</artifactId>
           </dependency>
           <dependency>
               <groupId>com.alibaba</groupId>
               <artifactId>druid-spring-boot-starter</artifactId>
           </dependency>
           <dependency>
               <groupId>org.mybatis.spring.boot</groupId>
               <artifactId>mybatis-spring-boot-starter</artifactId>
           </dependency>
           <dependency>
               <groupId>mysql</groupId>
               <artifactId>mysql-connector-java</artifactId>
           </dependency>
           <dependency>
               <groupId>org.springframework.boot</groupId>
               <artifactId>spring-boot-starter-jdbc</artifactId>
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

3. yml

   ```yml
   server:
     port: 2001
   
   spring:
     application:
       name: seata-order-service
     cloud:
       alibaba:
         seata:
           # 自定义事务组名称需要与seata-server中的对应
           tx-service-group: my_test_tx_group #因为seata的file.conf文件中没有service模块，事务组名默认为my_test_tx_group
           service:
             vgroupMapping:
               #要和tx-service-group的值一致
               my_test_tx_group: default
             grouplist:
               # seata seaver的 地址配置，此处可以集群配置是个数组
               default: 10.211.55.26:8091
       nacos:
         discovery:
           server-addr: 10.211.55.26:8848  #nacos
     datasource:
       # 当前数据源操作类型
       type: com.alibaba.druid.pool.DruidDataSource
       # mysql驱动类
       driver-class-name: com.mysql.cj.jdbc.Driver
       url: jdbc:mysql://10.211.55.26:3305/seata_order?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=GMT%2B8
       username: root
       password: 123456
   feign:
     hystrix:
       enabled: false
   logging:
     level:
       io:
         seata: info
   
   mybatis:
     mapperLocations: classpath*:mapper/*.xml
   ```

4. file.conf

   ```javascript
   server:
     port: 2001
   
   spring:
     application:
       name: seata-order-service
     cloud:
       alibaba:
         seata:
           # 自定义事务组名称需要与seata-server中的对应
           tx-service-group: my_test_tx_group #因为seata的file.conf文件中没有service模块，事务组名默认为my_test_tx_group
           service:
             vgroupMapping:
               #要和tx-service-group的值一致
               my_test_tx_group: default
             grouplist:
               # seata seaver的 地址配置，此处可以集群配置是个数组
               default: 10.211.55.26:8091
       nacos:
         discovery:
           server-addr: 10.211.55.26:8848  #nacos
     datasource:
       # 当前数据源操作类型
       type: com.alibaba.druid.pool.DruidDataSource
       # mysql驱动类
       driver-class-name: com.mysql.cj.jdbc.Driver
       url: jdbc:mysql://10.211.55.26:3305/seata_order?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=GMT%2B8
       username: root
       password: 123456
   feign:
     hystrix:
       enabled: false
   logging:
     level:
       io:
         seata: info
   
   mybatis:
     mapperLocations: classpath*:mapper/*.xml
   ```

5. registry.conf

   ```javascript
   registry {
     # file 、nacos 、eureka、redis、zk、consul、etcd3、sofa
     type = "nacos"
   
     nacos {
       application = "seata-server"
       serverAddr = "10.211.55.26:8848"    #nacos
       namespace = ""
       username = ""
       password = ""
     }
     eureka {
       serviceUrl = "http://localhost:8761/eureka"
       weight = "1"
     }
     redis {
       serverAddr = "localhost:6379"
       db = "0"
       password = ""
       timeout = "0"
     }
     zk {
       serverAddr = "127.0.0.1:2181"
       sessionTimeout = 6000
       connectTimeout = 2000
       username = ""
       password = ""
     }
     consul {
       serverAddr = "127.0.0.1:8500"
     }
     etcd3 {
       serverAddr = "http://localhost:2379"
     }
     sofa {
       serverAddr = "127.0.0.1:9603"
       region = "DEFAULT_ZONE"
       datacenter = "DefaultDataCenter"
       group = "SEATA_GROUP"
       addressWaitTime = "3000"
     }
     file {
       name = "file.conf"
     }
   }
   
   config {
     # file、nacos 、apollo、zk、consul、etcd3、springCloudConfig
     type = "file"
   
     nacos {
       serverAddr = "127.0.0.1:8848"
       namespace = ""
       group = "SEATA_GROUP"
       username = ""
       password = ""
     }
     consul {
       serverAddr = "127.0.0.1:8500"
     }
     apollo {
       appId = "seata-server"
       apolloMeta = "http://192.168.1.204:8801"
       namespace = "application"
     }
     zk {
       serverAddr = "127.0.0.1:2181"
       sessionTimeout = 6000
       connectTimeout = 2000
       username = ""
       password = ""
     }
     etcd3 {
       serverAddr = "http://localhost:2379"
     }
     file {
       name = "file.conf"
     }
   }
   ```

6. domain.CommonResult

   ```java
   @Data
   @AllArgsConstructor
   @NoArgsConstructor
   public class CommonResult<T> {
       private Integer code;
       private String message;
       private T data;
   
       public CommonResult(Integer code, String message) {
           this(code, message, null);
       }
   }
   ```

   domain.Account

   ```java
   @Data
   @AllArgsConstructor
   @NoArgsConstructor
   public class Account {
   
       private Long id;
   
       private Long userId;
   
       private BigDecimal total;
   
       private BigDecimal used;
   
       private BigDecimal  residue;
   }
   ```
   
7. dao.AccountDao

   ```java
   @Mapper
   public interface AccountDao {
   
       void decrease(@Param("userId") Long userId, @Param("money") BigDecimal money);
   
   }
   ```

8. mapper.AccountMapper.xml

   ```xml
   <?xml version="1.0" encoding="UTF-8" ?>
   <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
           "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
   <mapper namespace="com.angenin.springcloud.dao.AccountDao">
   
       <resultMap id="BaseResultMap" type="com.angenin.springcloud.domain.Account">
           <id column="id" property="id" jdbcType="BIGINT"/>
           <result column="user_id" property="userId" jdbcType="BIGINT"/>
           <result column="total" property="total" jdbcType="DECIMAL"/>
           <result column="used" property="used" jdbcType="DECIMAL"/>
           <result column="residue" property="residue" jdbcType="DECIMAL"/>
       </resultMap>
   
       <update id="decrease">
           update t_account
           set used = used + #{money}, residue = residue - #{money}
           where user_id = #{userId};
       </update>
   </mapper>
   ```

9. service.AccountService

   ```java
   public interface AccountService {
       void decrease(Long userId, BigDecimal money);
   }
   ```

10. service.impl.AccountServiceImpl

   ```java
   @Service
   public class AccountServiceImpl implements AccountService {
   
       private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceImpl.class);
   
       @Resource
       private AccountDao accountDao;
   
       @Override
       public void decrease(Long userId, BigDecimal money) {
           LOGGER.info("---> AccountService中扣减账户余额");
           //模拟超时异常，暂停20秒
           try {
               TimeUnit.SECONDS.sleep(20);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
           accountDao.decrease(userId, money);
           LOGGER.info("---> AccountService中扣减账户余额完成");
       }
   }
   ```

11. controller.AccountController

    ```java
    @RestController
    public class AccountController {
    
        @Resource
        private AccountService accountService;
    
        @RequestMapping("/account/decrease")
        public CommonResult decrease(@RequestParam("userId") Long userId, @RequestParam("money") BigDecimal money){
            accountService.decrease(userId, money);
            return new CommonResult(200, "扣减库存成功!");
        }
    
    }
    ```

12. config.MybatisConfig

    ```java
    @MapperScan("com.xiaotu.cloud.dao")
    @Configuration
    public class MybatisConfig {
    }
    ```

    config.DataSourceProxyConfig

    ```java
    
    @Configuration
    public class DataSourceProxyConfig {
    
        @Value("${mybatis.mapperLocations}")
        private String mapperLocations;
    
        @Bean
        @ConfigurationProperties(prefix = "spring.datasource")
        public DataSource druidDataSource() {
            return new DruidDataSource();
        }
    
        @Bean
        public DataSourceProxy dataSourceProxy(DataSource druidDataSource) {
            return new DataSourceProxy(druidDataSource);
        }
    
        @Bean
        public SqlSessionFactory sqlSessionFactoryBean(DataSourceProxy dataSourceProxy) throws Exception {
            SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
            bean.setDataSource(dataSourceProxy);
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            bean.setMapperLocations(resolver.getResources(mapperLocations));
            return bean.getObject();
        }
    }
    ```

13. 主启动类

    ```java
    @SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
    @EnableFeignClients
    @EnableDiscoveryClient
    public class SeataAccountMain2003 {
    
        public static void main(String[] args) {
            SpringApplication.run(SeataAccountMain2003.class,args);
        }
    }
    ```

14. 启动2003

    
