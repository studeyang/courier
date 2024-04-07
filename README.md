# 01 简介

Courier 是基于 Kafka 的微服务化的消息总线，可发送、消费、监控消息，提供了 Java 客户端, 支持异构系统接入。

# 02 快速开始

### 2.1 发送一条消息

**1. 引入 courier-core 依赖**

```xml
<dependency>
    <groupId>io.github.studeyang</groupId>
    <artifactId>courier-core</artifactId>
    <optional>true</optional>
    <!-- 版本由dependencies工程管理 -->
</dependency>
```

**2. 定义一个消息**

```java
@Data
@Topic(name = "user")
public class UserCreated extends Event {
 
    private String name;
 
    private Integer age;
 
    private Date createdAt;
}
```

**3. 发送这条消息**

```java
public void sendMessage() {
    Event event = new UserCreated();
    EventPublisher.publish(event);
}
```

### 2.2 接收一条消息

**1. 引入 courier-spring-boot-starter 依赖**

```xml
<dependency>
    <groupId>io.github.studeyang</groupId>
    <artifactId>courier-spring-boot-starter</artifactId>
    <!-- 版本由dependencies工程管理 -->
</dependency>
```

**2. 启动类添加 @EnableMessage**

```java
@EnableMessage
@SpringBootApplication
public class WebApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}
```

启动后, 出现banner, 意味着courier启动成功。

![image-20201113101624074](https://technotes.oss-cn-shenzhen.aliyuncs.com/2021/images/image-20201113101624074.png)

**3. 定义一个消息接收器**

```java
@Component
@EventHandler(topic = "user", consumerGroup = "user")
public class UserMessageHandler {
 
    public void handle(UserCreated userCreated) {
        System.out.println("user created: " + userCreated);
    }

}
```

- `@EventHandler`：表示该类是个消息处理器；
- `topic`：订阅`topic`为`user`的消息；
- `consumerGroup`：消费组；

# 03 软件架构

### 3.1 架构流程

![image-20201103100523718](https://technotes.oss-cn-shenzhen.aliyuncs.com/2021/images/image-20201103100523718.png)

流程说明：

1. 消息的生产方从引用的`courier-client.jar`发送到`Producer`服务, `Producer`再发送到`Kafka`；
2. `Consumer`服务从`Kafka` `poll`消息并推送到消费方的`Client`端；
3. 成功/失败的消息都会报告给`Management`, 人工可通过`Management`进行再生产、再消费、监控 ;
