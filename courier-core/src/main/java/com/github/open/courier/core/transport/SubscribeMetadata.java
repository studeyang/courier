package com.github.open.courier.core.transport;

import com.github.open.courier.core.listener.ListenerConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 订阅关系元数据
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(exclude = {"env", "envTag"})
public class SubscribeMetadata implements ListenerConfig {

    /**
     * 服务所属集群名
     */
    private String cluster;

    /**
     * 集群名 / 指定消费环境标识
     */
    private String env;

    /**
     * 订阅的服务
     */
    private String service;

    /**
     * 订阅的topic
     */
    private String topic;

    /**
     * 消费组ID
     */
    private String groupId;

    /**
     * 订阅的事件类型
     */
    private String type;

}
