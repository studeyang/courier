package com.github.open.courier.core.transport;

import java.util.Date;

import com.github.open.courier.core.message.Usage;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@ApiModel(description = "发送失败的消息")
@Data
@Accessors(chain = true)
public class SendFailMessage {

    @ApiModelProperty("消息ID")
    private String messageId;

    @ApiModelProperty("消息的topic")
    private String topic;

    @ApiModelProperty("消息类型")
    private String type;

    @ApiModelProperty("消息发送方的cluster")
    private String cluster;

    @ApiModelProperty("消息发送方的env")
    private String env;

    @ApiModelProperty("消息发送方的service")
    private String service;

    @ApiModelProperty("消息体")
    private String content;

    @ApiModelProperty("消息的创建时间")
    private Date createdAt;

    @ApiModelProperty("消息主键")
    private String primaryKey;

    @ApiModelProperty("用途")
    private Usage usage;

    @ApiModelProperty("消息的重试发送次数")
    private Integer retries;

    @ApiModelProperty("失败原因")
    private String reason;
}
