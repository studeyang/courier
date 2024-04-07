package com.github.open.courier.core.transport;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "订阅结果")
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SubscribeResult {

    @ApiModelProperty("是否订阅成功")
    private boolean success;

    @ApiModelProperty("原因")
    private String reason;

    public static SubscribeResult success(String reason) {
        return new SubscribeResult(true, reason);
    }

    public static SubscribeResult error(String reason) {
        return new SubscribeResult(false, reason);
    }
}
