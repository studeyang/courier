package com.github.open.courier.core.transport;

import com.github.open.courier.core.message.Usage;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 2 * @Author: chengyan
 * 3 * @Date: 2020/12/3 9:35
 */
@Data
@Accessors(chain = true)
public class DelayMessage {

    /**
     * 消息id
     */
    private String messageId;

    /**
     * 主题
     */
    private String topic;

    /**
     * 消息类型全写
     */
    private String type;

    /**
     * 消息发送方的cluster
     */
    private String cluster;

    /**
     * 消息发送方的env
     */
    private String env;

    /**
     * 消息发送方的service
     */
    private String fromService ;

    /**
     * 消息体
     */
    private String content;

    /**
     * 消息创建时间
     */
    private Date createdAt;

    /**
     * 消息主键
     */
    private String primaryKey;

    /**
     * 用途
     */
    private Usage usage;

    /**
     * 重试次数
     */
    private Integer retries;

    /**
     * 到期时间戳
     */
    private Long expireTime;

    /**
     * 是否已读
     */
    private Boolean isPreread;

    /**
     * 是否发送
     */
    private Boolean isSend;

    /**
     * 开始投递时间
     */
    private Date startDeliveTime;

    /**
     * 结束投递时间
     */
    private Date endDeliveTime;
}
