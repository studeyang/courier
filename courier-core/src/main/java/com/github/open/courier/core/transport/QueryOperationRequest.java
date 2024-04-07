package com.github.open.courier.core.transport;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 2 * @Author: chengyan
 * 3 * @Date: 2020/9/17 17:26
 */
@Data
@ApiModel(description = "消息重试实体类")
public class QueryOperationRequest {


    @ApiModelProperty(value = "ids")
    private List<String> ids;

    @ApiModelProperty(value = "开始时间")
    private String startTime;

    @ApiModelProperty(value = "结束时间")
    private String endTime;

}
