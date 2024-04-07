package com.github.open.courier.core.transport;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 消费记录
 */
@Data
@Accessors(chain = true)
public class ConsumeRecord {

    /**
     * 消费ID
     */
    private String id;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 发送方的服务
     */
    private String fromService;

    /**
     * 消费方的服务
     */
    private String toService;

    /**
     * 消息的主题
     */
    private String topic;

    /**
     * 消费组ID
     */
    private String groupId;

    /**
     * 重试次数
     */
    private Integer retries;

    /**
     * 消费状态
     */
    private ConsumeState state;

    /**
     * 是否需要重推（消费超时重推）
     */
    private Boolean needRepush;

    /**
     * 从kakfa拉取到消息的时间
     */
    private Date pollTime;

    /**
     * 开始往客户端推送消息的时间
     */
    private Date beforePushTime;

    /**
     * 推送结束时间
     */
    private Date endPushTime;

    /**
     * client接收到消息的时间
     */
    private Date clientReceiveTime;

    /**
     * client处理结束返回的时间
     */
    private Date clientEndTime;

    /**
     * client处理消息花费的时长
     */
    private long clientHandledCost;

}
