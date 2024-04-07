package com.github.open.courier.core.transport;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@ApiModel(description = "线程池告警元数据")
@Data
@Accessors(chain = true)
public class ThreadPoolAlarmMetadata {

    @ApiModelProperty("节点IP地址")
    private String nodeIp;

    @ApiModelProperty("队列容量")
    private Integer queueCapacity;

    @ApiModelProperty("告警阈值")
    private Integer pauseThreshold;

    @ApiModelProperty("恢复阈值")
    private Integer resumeThreshold;

    @ApiModelProperty("队列大小")
    private Integer queueSize;
}
