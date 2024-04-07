package com.github.open.courier.admin.aop;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 在方法上加上该注解, 可以打印入参、出参和方法执行时长
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Log {

}
