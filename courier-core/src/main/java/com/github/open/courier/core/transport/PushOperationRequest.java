package com.github.open.courier.core.transport;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 暂停推送请求
 *
 * @author yangllu
 */
@ApiModel(description = "暂停推送请求")
@Data
@Accessors(chain = true)
public class PushOperationRequest {

    @ApiModelProperty("操作（PAUSE, RESUME）")
    private PushOperation operation;

    @ApiModelProperty(value = "ip地址", example = "10.0.0.1")
    private String ip;

    @ApiModelProperty(value = "端口号", example = "8080")
    private Integer port;

    @ApiModelProperty(value = "服务名", example = "courier-consumer")
    private String service;

    public enum PushOperation {
        /**
         * 推送操作
         */
        PAUSE, RESUME
    }

}
