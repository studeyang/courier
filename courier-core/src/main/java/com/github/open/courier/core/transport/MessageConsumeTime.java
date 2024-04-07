package com.github.open.courier.core.transport;

import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "消费成功消息的时间戳")
@Data
@NoArgsConstructor
@AllArgsConstructor
public final class MessageConsumeTime {

    @ApiModelProperty("消费ID")
    private String id;

    @ApiModelProperty("client接收时间")
    private Date clientReceiveTime;

    @ApiModelProperty("client消费完成时间")
    private Date clientEndTime;

    @ApiModelProperty("消费耗时")
    private long clientHandledCost;

    @ApiModelProperty("拉取时间（避免全表扫描）")
    private Date pollStartTime;

    @ApiModelProperty("拉取时间（避免全表扫描）")
    private Date pollEndTime;
}
