package com.github.open.courier.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.github.open.courier.autoconfigure.CourierClientAutoConfiguration;

@Retention(RUNTIME)
@Target(TYPE)
@Import(CourierClientAutoConfiguration.class)
public @interface EnableMessage {}
