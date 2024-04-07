package com.github.open.courier.core.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 标记在Message的字段上, 用于指定该消息的主键, 相同主键值的消息将会被顺序消费
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface PrimaryKey {

}
