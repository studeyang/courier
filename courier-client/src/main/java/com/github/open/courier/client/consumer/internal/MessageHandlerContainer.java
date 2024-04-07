package com.github.open.courier.client.consumer.internal;

import com.github.open.courier.core.support.CourierContext;
import com.github.open.courier.core.exception.SubscribeException;
import com.github.open.courier.core.support.CourierVersion;
import com.github.open.courier.core.transport.SubscribeRequest;
import com.github.open.courier.core.transport.TopicGroup;
import com.github.open.courier.annotation.EventHandler;
import com.github.open.courier.messaging.Message;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * MessageHandler容器
 */
@Slf4j
@Getter
public class MessageHandlerContainer implements BeanPostProcessor {

    private final SubscribeRequest subscribeRequest;
    private final Map<Class<?>, MessageHandler> handlerMap;

    public MessageHandlerContainer() {
        this.handlerMap = Maps.newHashMap();
        this.subscribeRequest = new SubscribeRequest()
                .setService(CourierContext.getService())
                .setDevEnvironment(CourierContext.isDevEnvironment())
                .setTopicGroups(Sets.newHashSet())
                .setClientVersion(CourierVersion.get());
    }

    /**
     * 在Spring初始完每个bean后, 判断是否是消息接收类并收集
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {

        EventHandler eventHandler = AnnotationUtils.findAnnotation(bean.getClass(), EventHandler.class);

        if (eventHandler == null || eventHandler.ignore()) {
            return bean;
        }

        String topic = eventHandler.topic().trim();
        String groupId = eventHandler.consumerGroup().trim();

        if (StringUtils.isEmpty(topic) || StringUtils.isEmpty(groupId)) {
            throw new SubscribeException(bean.getClass().getName() + " 上的@EventHandler的topic和consumerGroup不能为空");
        }

        // 不应该用getDeclaredMethods(), 应该扫描[public]的方法
        Method[] methods = bean.getClass().getMethods();

        for (Method method : methods) {

            if (!MessageHandler.isHandleMethod(method)) {
                continue;
            }

            Class<?> messageClass = method.getParameterTypes()[0];

            accumulate(messageClass, topic, groupId);

            MessageHandler handler = new MessageHandler(bean, method);
            if (handlerMap.putIfAbsent(messageClass, handler) == null) {
                log.info("kafka处理器: {}", handler);
            } else {
                log.warn("kafka已经绑定了处理器: {}, 忽略: {}", handlerMap.get(messageClass), handler);
            }
        }

        return bean;
    }

    /**
     * 将该type累加到topic和groupId对应的TopicGroup中(一个listener下所需的types)
     */
    private void accumulate(Class<?> messageClass, String topic, String groupId) {

        Set<TopicGroup> topicGroups = subscribeRequest.getTopicGroups();

        Optional<TopicGroup> topicGroup = topicGroups.stream()
                .filter(tp -> tp.getTopic().equals(topic) && tp.getGroupId().equals(groupId))
                .findFirst();

        if (topicGroup.isPresent()) {
            topicGroup.get().getTypes().add(messageClass.getName());
        } else {
            TopicGroup tp = new TopicGroup()
                    .setTopic(topic)
                    .setGroupId(groupId)
                    .setTypes(Sets.newHashSet(messageClass.getName()));
            topicGroups.add(tp);
        }
    }

    /**
     * 通过 Message 获取 MessageHandler
     */
    public MessageHandler getHandler(Message message) {
        return handlerMap.get(message.getClass());
    }

    /**
     * 在每个bean初始化前执行的操作
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }
}
