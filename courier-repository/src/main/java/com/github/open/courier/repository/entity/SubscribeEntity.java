package com.github.open.courier.repository.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/5/31
 */
@Data
@Accessors(chain = true)
public class SubscribeEntity {

    /**
     * 服务所属集群
     */
    private String cluster;

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

    /**
     * 环境标识
     */
    private String envTag;

    /**
     * 订阅时间
     */
    private Date subscribedAt;

}