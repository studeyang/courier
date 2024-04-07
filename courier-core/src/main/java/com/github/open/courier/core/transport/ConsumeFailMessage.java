package com.github.open.courier.core.transport;

import java.util.Date;

import com.github.open.courier.core.message.Usage;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@ApiModel(description = "消费失败的消息")
@Data
@Accessors(chain = true)
public class ConsumeFailMessage {

    @ApiModelProperty("消费ID")
    private String id;

    @ApiModelProperty("消息ID")
    private String messageId;

    @ApiModelProperty("Topic")
    private String topic;

    @ApiModelProperty("消息类型")
    private String type;

    @ApiModelProperty("消费GroupId")
    private String groupId;

    @ApiModelProperty("消费的服务")
    private String service;

    @ApiModelProperty("创建时间")
    private Date createdAt;

    @ApiModelProperty("主键")
    private String primaryKey;

    @ApiModelProperty("用途")
    private Usage usage;

    @ApiModelProperty("重试次数")
    private Integer retries;

    @ApiModelProperty("失败原因")
    private String reason;

    @ApiModelProperty("是否需要自动重推")
    private Boolean needRepush;

    /**
     * 找表分片需要
     */
    private Date pollTime;
}
