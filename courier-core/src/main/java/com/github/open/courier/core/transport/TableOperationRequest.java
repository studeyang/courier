package com.github.open.courier.core.transport;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 表操作
 *
 * @author yanglulu
 */
@Data
@Accessors(chain = true)
@ApiModel(description = "表操作")
public class TableOperationRequest {

    @ApiModelProperty(value = "表名")
    private String table;

    @ApiModelProperty(value = "表后缀（以“_”开头）")
    private List<String> suffixList;

}