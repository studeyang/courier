package com.github.open.courier.client.consumer.internal;

import com.github.open.courier.messaging.Message;
import io.github.open.toolkit.commons.exception.HttpMessageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 方法消息处理器, 通过反射调用自定义的消息处理方法
 * 叫ReflectionMessageHandler(反射消息处理器)会不会好点?
 */
@Slf4j
@RequiredArgsConstructor
public class MessageHandler {

    private final Object object;
    private final Method method;

    /**
     * 通过反射调用定义的消息处理方法
     */
    public void invoke(Message message) throws Exception {

        try {
            method.invoke(object, message);
        } catch (InvocationTargetException e) {
            // 如果抛出HttpMessageException, 就当做消费成功, 不再重试
            if (e.getCause() instanceof HttpMessageException) {
                log.warn("kafka消费结束, 有HttpMessageException异常抛出, mid:{}, e:{}", message.getId(), e.getCause());
            } else {
                throw e;
            }
        }
    }

    @Override
    public String toString() {
        return name(method);
    }

    /**
     * 判断是否是消息处理方法
     */
    static boolean isHandleMethod(Method method) {

        final String suggestedHandleMethodName = "handle";

        Class<?>[] parameterTypes = method.getParameterTypes();

        boolean isHandleMethod = parameterTypes.length == 1 && Message.class.isAssignableFrom(parameterTypes[0]);

        // 有些组不是用handle作为方法名的, 要做兼容, 给个警告提示
        if (isHandleMethod && !suggestedHandleMethodName.equals(method.getName())) {
            log.warn("建议将kafka处理器: {}的方法名改为{}", name(method), suggestedHandleMethodName);
        }

        return isHandleMethod;
    }

    static String name(Method method) {
        return method.getDeclaringClass().getName() + "#" + method.getName() + "(" + method.getParameterTypes()[0].getSimpleName() + ")";
    }
}
