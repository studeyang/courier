package com.github.open.courier.core.transport;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(description = "订阅集群元数据实体")
public class SubscribeClusterDTO {

	@ApiModelProperty("集群")
	private String cluster;

	@ApiModelProperty("环境")
	private String env;

	@ApiModelProperty("推送地址")
	private String url;
}
