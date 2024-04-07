package com.github.open.courier.core.transport;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(description = "订阅元数据查询请求")
public class QuerySubscribeMetaDataRequest {

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

	@ApiModelProperty("是否灰度")
	private Boolean isGray;

	@ApiModelProperty("当前页")
	private Integer page = 1;

	@ApiModelProperty("每页大小")
	private Integer size = 10;
}
