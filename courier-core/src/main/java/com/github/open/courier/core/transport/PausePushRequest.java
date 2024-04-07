package com.github.open.courier.core.transport;

import com.github.open.courier.core.constant.ClientState;
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
public class PausePushRequest {

    @ApiModelProperty("ip地址")
    private String ip;

    @ApiModelProperty("端口号")
    private Integer port;

    @ApiModelProperty("服务名")
    private String service;

    @ApiModelProperty("客户端状态")
    private ClientState clientState;

}
