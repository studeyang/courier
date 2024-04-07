package com.github.open.courier.core.transport;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;

@ApiModel(description = "订阅消息请求")
@Data
@Accessors(chain = true)
public class SubscribeRequest {

    @ApiModelProperty("集群名")
    private String cluster;

    @ApiModelProperty("环境名")
    private String env;

    @ApiModelProperty("服务名")
    private String service;

    @ApiModelProperty("是否是开发环境")
    private boolean devEnvironment;

    @ApiModelProperty("订阅的Topic和Group")
    private Set<TopicGroup> topicGroups;

    @ApiModelProperty("客户端版本号")
    private String clientVersion;

    // TODO remove it

    @ApiModelProperty("消费模式")
    private Mode mode;

    @ApiModelProperty("消费消息的url")
    private String url;
}
