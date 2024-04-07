# 01 简介

Courier 是基于 Kafka 的企业级消息总线，可发送、消费、监控消息，实现了跨系统的消息通信，并提供了 Java 客户端，支持异构系统接入。

# 02 架构

## 2.1 生产消费

![生产消费](https://technotes.oss-cn-shenzhen.aliyuncs.com/2023/image-20240407163049838.png)

## 2.2 消息管理中心

![消息管理中心](https://technotes.oss-cn-shenzhen.aliyuncs.com/2023/image-20240407163124268.png)

## 2.3 部署架构

![部署架构](https://technotes.oss-cn-shenzhen.aliyuncs.com/2023/image-20240407163238282.png)

# 03 快速开始

## 3.1 发送一条消息

**1. 引入 courier-core 依赖**

```xml
<dependency>
    <groupId>io.github.studeyang</groupId>
    <artifactId>courier-core</artifactId>
    <optional>true</optional>
    <!-- 版本由dependencies工程管理 -->
</dependency>
```

**2. 定义一条消息**

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

## 3.2 接收一条消息

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


