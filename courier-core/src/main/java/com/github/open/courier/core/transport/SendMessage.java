package com.github.open.courier.core.transport;

import com.github.open.courier.core.message.Usage;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * @author Courier
 */
@ApiModel(description = "发送消息")
@Data
@Accessors(chain = true)
public class SendMessage {

    @ApiModelProperty(value = "消息ID", required = true, example = "123456789")
    private String messageId;

    @ApiModelProperty(value = "消息的topic", required = true, example = "alpha-user")
    private String topic;

    @ApiModelProperty(value = "消息类型", required = true, example = "com.xxx.ec.cloud.events.user.UserCreated")
    private String type;

    @ApiModelProperty(value = "消息发送方的service", required = true, example = "user-service")
    private String service;

    @ApiModelProperty(value = "发往kafka的消息体(String类型的json字符串, 注意不要有[换行符])", required = true)
    private String content;

    @ApiModelProperty(value = "消息的创建时间", required = true, example = "1590118641916")
    private Date createdAt;

    @ApiModelProperty(value = "用途", required = true, example = "EVENT")
    private Usage usage;

    @ApiModelProperty(value = "消息主键", example = "null")
    private String primaryKey;

    /**
     * retries 不需要序列化后传输到producer
     */
    @JsonIgnore
    @ApiModelProperty("消息的重试发送次数")
    private Integer retries;

    @ApiModelProperty("消息的延迟发送时间")
    private Long delayMillis;

    @ApiModelProperty(example = "cassmall")
    private String cluster;

    @ApiModelProperty(example = "kunlun")
    private String env;

    /**
     * 获取发送kafka时的key, 如果是有主键则使用主键, 否则用id
     */
    @JsonIgnore
    public String getKey() {
        return StringUtils.defaultIfEmpty(primaryKey, messageId);
    }

    public SendMessage addRetries(int retries) {
        this.retries += retries;
        return this;
    }
}
