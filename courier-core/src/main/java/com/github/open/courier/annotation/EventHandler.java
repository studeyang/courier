package com.github.open.courier.annotation;

import java.lang.annotation.*;

/**
 * 标记在类上, 用于指定该类是一个消息处理器
 * 该类下的[有且只有一个参数]且[是com.xxx.courier.core.message.Message类型]且[方法名为"handle"]的方法, 将接收指定类型的消息
 *
 * @author Courier
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler {

    /**
     * 接收指定topic的消息
     */
    String topic();

    /**
     * 使用指定的consumerGroup接收消息
     */
    String consumerGroup();

    /**
     * 是否忽略, 默认false, 如果为true, 则该类将被忽略
     */
    boolean ignore() default false;
}
