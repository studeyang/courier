package com.github.open.courier.core.listener;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Listener配置
 *
 * @author Courier
 */
public interface ListenerConfig {

    /**
     * 获取Topic
     *
     * @return topic
     */
    String getTopic();

    /**
     * 获取GroupId
     *
     * @return gourpId
     */
    String getGroupId();

    /**
     * listener的name, 不需要序列化
     *
     * @return topic&groupId
     */
    @JsonIgnore
    default String getName() {
        return getTopic() + " | " + getGroupId();
    }
}
