package com.github.open.courier.core.converter;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Objects;

import com.github.open.courier.core.support.Wrapper;
import org.apache.commons.lang3.StringUtils;

import com.github.open.courier.core.annotation.PrimaryKey;
import com.github.open.courier.core.exception.NotTopicException;
import com.github.open.courier.annotation.Topic;
import com.github.open.courier.messaging.Message;
import com.google.common.collect.Maps;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * 消息内省, 用于获取Message的自身属性
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageIntrospector {

    /**
     * topic缓存, K-Class<Message>, V-topic
     */
    private static final Map<Class<?>, String> topicCache = Maps.newConcurrentMap();

    /**
     * primaryKeyField缓存, K-Class<Message>, V-primaryKeyField
     */
    private static final Map<Class<?>, Field> primaryKeyFieldCache = Maps.newConcurrentMap();

    /**
     * 如果primaryKeyFieldCache中的value == noPrimaryKeyField, 则代表该Class没有primaryKeyField, 即没有@PrimaryKey
     */
    private static final Field noPrimaryKeyField = noPrimaryKeyField();

    @SneakyThrows
    private static Field noPrimaryKeyField() {
        return MessageIntrospector.class.getDeclaredField("noPrimaryKeyField");
    }

    /**
     * 获取topic
     */
    public static String getTopic(Class<?> clazz) {
        return topicCache.computeIfAbsent(clazz, MessageIntrospector::getTopicFromAnnotation);
    }

    /**
     * 从Class中获取topic
     */
    private static String getTopicFromAnnotation(Class<?> clazz) {

        Topic topic = clazz.getAnnotation(Topic.class);

        if (null == topic || StringUtils.isBlank(topic.name())) {
            throw new NotTopicException(MessageFormat.format("该消息:{0} 找不到topic", clazz.getName()));
        }

        return Wrapper.wrapTopic(topic.name());
    }

    /**
     * 获取message的sequenceKey
     */
    public static String getPrimaryKey(Message message) {

        Field primaryKeyField = primaryKeyFieldCache.computeIfAbsent(message.getClass(), MessageIntrospector::getPrimaryKeyField);

        if (primaryKeyField == noPrimaryKeyField) {
            return null;
        }

        try {
            return Objects.toString(primaryKeyField.get(message), null);
        } catch (ReflectiveOperationException e) {
            log.error("反射获取Field的值失败, Field:{}, message:{}", primaryKeyField, message, e);
            return null;
        }
    }

    /**
     * 获取带有@PrimaryKey的Field
     */
    private static Field getPrimaryKeyField(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(PrimaryKey.class)) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                return field;
            }
        }
        return noPrimaryKeyField;
    }
}
