# 二十、SpringCloud Alibaba Seata 处理分布式事务



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

下载：https://github.com/seata/seata/releases ( 新版的用spring写的 配置走application.yml)

1. 先备份原始的file.conf文件

2. 自定义事务组名称+事务日志存储模式为db+数据库连接想你想

3. file.conf   service模块  store模块 (新版看这里https://seata.io/zh-cn/docs/user/configurations.html)

   ```sh
   ### service
   service {
       vgroup_mappingmy_test_tx_group = "fsp_tx_group
       
       default.grouplist ="127.0.0.1:8091
       enableDegrade = false
       disable = false
       max.commit.retry.timeout = "-1
       max.rollback.retry.timeout = "-1
   }
   
   ### store
   store {
   ## store mode: file、db
   	mode = "db"
   }
   
   ### db
   db {
   ## the implement of javaxsql.DataSource, such as DruidData
   datasource = "dbcp"
   ## mysql/oracle/h2/oceanbase etc.
   db-type = "mysql"
   driver-class-name ="com.mysql.jdbc.Driver"
   url =  "jdbc:mysql://127.0.0.1:3306/seata"
   user = "root"
   password = "你自己密码"
   min-conn = 1
   }
   
   
   docker run --name seata-server \
           -p 8091:8091 \
           -e SEATA_PORT=8091 \
           seataio/seata-server:1.5.0
   ```

4. mysql新建数据库 seata

5. seata库创建表

   5.1 seata\conf\application.conf配置

   ```yaml
   registry:
   	nacos
   ```

   

   

## 订单/库存/账户业务微服务准备

### 订单模块

1. 新建模块seata-order-service2001
2. 


