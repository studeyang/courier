package com.github.open.courier.core.transport;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(description = "清除消息推送环境请求")
public class ClearMessagePushEnvRequest {

	@ApiModelProperty("服务所属集群名")
	private String cluster;

	@ApiModelProperty("订阅的服务")
	private String service;

	@ApiModelProperty("消费模式")
	private String type;
}
