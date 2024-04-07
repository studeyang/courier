package com.github.open.courier.core.transport;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Set;

/**
 * 订阅管理页面数据
 *
 * @author lIJIAHAO
 */
@Data
@Accessors(chain = true)
public class SubscribePageDTO {

    /**
     * 订阅的服务
     */
    private String service;

    /**
     * 订阅的topic集合
     */
    private Set<String> topics;

    /**
     * 消费组ID集合
     */
    private Set<String> groupIds;

    /**
     * 消息类型
     */
    private Set<String> types;

    /**
     * 接收消息的url
     */
    private String url;

    /**
     * 服务节点在线ip和端口
     */
    private List<String> ipAndPort;

    /**
     * 指定消息消费节点
     */
    private String consumerNode;

    /**
     * 是否启动
     */
    private boolean enable;

}
