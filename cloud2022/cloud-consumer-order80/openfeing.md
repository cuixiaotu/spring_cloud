# OpenFeign服务接口调用



简介

官网文档：https://cloud.spring.io/spring-cloud-static/Hoxton.SR1/reference/htmlsingle/#spring-cloud-openfeign

Feign是一个声明式WebService服务器，使用Feign能让编写Web Service客户端更加简单。定义一个服务接口然后在上面添加注解。Feign也支持可拔插式的编码器和解码器。Spring Cloud对Feign进行封装，使支持Spring MVC标注注解和HttpMessageConverters。Feign可以对Eureka和Ribbon组合使用以支持负载均衡。



前面在使用Ribbon+RestTemplate时，利用RestTempalte对http请求的封装处理，形成了一套模板式的调用方法。实际使用中。由于对服务依赖的调用可能不止一处，往往一个接口会被多处调用，所以通常会针对每个微服务自行封装一些客户端来包装这些服务的调用。所以Feign在此基础上进一步封装，由他来帮助我们定义和实现服务接口的定义，在Feign的实现下，我们只需要创建一个接口并使用注解的方式来配置它（以前是Dao接口上标注Mapper注解，现在是一个微服务接口上面标注一个Feign注解即可），即可完成对服务提供方的接口绑定，简化使用了Spring cloud Ribbon时，自动封装服务调用客户端的开发量。



Feign和OpenFeign的区别

| Feign                                                        | OpenFeign                                                    |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| Feign是Spring Cloud组件中一个轻量级RESTful的HTTP服务客户端。Feign内置了Ribbon,用来做客户端负载均衡，去调用服务注册中心的服务。Feign的使用方式：使用Feign的注解定义接口，调用这个接口，就可以调用服务注册中心的服务 | OpenFeign是Spring Cloud在Feign的基础上支持了SpringMVC的注解，如RequestMapping等。OpenFeign的@FeignClient可以解析SpringMVC的@RequestMapping注解下的接口，并通过动态代理方式产生实现类，实现类中做负载均衡并调用其他服务。 |
| spring-cloud-start-feign                                     | Spring-cloud-start-openfeign                                 |



![img](https://img-blog.csdnimg.cn/20200607203046585.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM2OTAzMjYx,size_16,color_FFFFFF,t_70)



