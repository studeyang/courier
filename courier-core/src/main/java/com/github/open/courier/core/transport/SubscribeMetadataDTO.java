package com.github.open.courier.core.transport;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(description = "订阅元数据实体")
public class SubscribeMetadataDTO {

	@ApiModelProperty("服务所属集群名")
	private String cluster;

	@ApiModelProperty("订阅的服务")
	private String service;

	@ApiModelProperty("订阅的topic")
	private String topic;

	@ApiModelProperty("消费组ID")
	private String groupId;

	@ApiModelProperty("订阅的事件类型")
	private String type;

	@ApiModelProperty("环境标识")
	private String envTag;
}
