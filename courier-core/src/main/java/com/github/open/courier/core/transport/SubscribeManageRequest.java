package com.github.open.courier.core.transport;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@ApiModel(description = "订阅服务管理请求")
@Data
@Accessors(chain = true)
public class SubscribeManageRequest {

    @ApiModelProperty("服务名")
    String service;

    @ApiModelProperty("订阅服务指定消费节点")
    String consumerNode;

    @ApiModelProperty("是否可用")
    String enable;


}
