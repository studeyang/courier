package com.github.open.courier.admin.aop;

import com.github.open.courier.core.converter.MessageJsonConverter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class MonitoringActuator {

    /**
     * 打印含有@Log注解的方法的方法名、入参、出参、执行时长
     */
    @Around("@annotation(com.github.open.courier.admin.aop.Log)")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {

        long begin = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        log.info("method:{}, cost:{}ms, args:{}",
                 joinPoint.getSignature().getName(),
                 System.currentTimeMillis() - begin,
                 MessageJsonConverter.toJson(joinPoint.getArgs()));

        return result;
    }
}
