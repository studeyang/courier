package com.github.open.courier.core.support;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

/**
 * 包装器
 */
public final class Wrapper {

    /**
     * 当前环境
     */
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private static String profile;

    /**
     * topic前缀
     */
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private static String topicPrefix;

    /**
     * groupId前缀
     */
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private static String groupIdPrefix;

    @Value("${courier.topic-prefix}")
    private void init(String prefix) {
        Assert.notNull(prefix, "未识别到 ${courier.topic-prefix} 值");
        // 初始化一遍
        if (StringUtils.isEmpty(topicPrefix)) {
            customizeTopicPrefix(prefix);
        }
    }

    public void customizeTopicPrefix(String topicPrefix) {
        setProfile(topicPrefix);
        setTopicPrefix(topicPrefix + '-');
        setGroupIdPrefix("CID_alikafka_" + topicPrefix.toUpperCase() + '_');
    }

    /**
     * 添加topic的前缀
     */
    public static String wrapTopic(String topic) {
        return topicPrefix.concat(topic.trim());
    }

    /**
     * 添加group的前缀
     */
    public static String wrapGroupId(String groupId) {
        return groupIdPrefix.concat(groupId.trim().toUpperCase());
    }

    /**
     * 去除topic的前缀
     */
    public static String unWrapTopic(String topic) {

        if (isWrappedTopic(topic)) {
            return topic.substring(topicPrefix.length());
        }
        // 去除失败, 直接返回
        return topic;
    }

    /**
     * 去除groupId的前缀
     */
    public static String unWrapGroupId(String groupId) {

        if (isWrappedGroupId(groupId)) {
            // groupId的大小写已经丢失了, 统一返回小写, 没什么影响
            return groupId.substring(groupIdPrefix.length()).toLowerCase();
        }
        // 去除失败, 直接返回
        return groupId;
    }

    /**
     * 判断topic是否已经加了前缀
     */
    public static boolean isWrappedTopic(String topic) {
        return topic.startsWith(topicPrefix);
    }

    /**
     * 判断groupId是否已经加了前缀
     */
    public static boolean isWrappedGroupId(String groupId) {
        return groupId.startsWith(groupIdPrefix);
    }
}
