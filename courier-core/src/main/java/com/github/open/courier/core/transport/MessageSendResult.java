package com.github.open.courier.core.transport;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Courier
 */
@ApiModel(description = "发送消息的结果")
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class MessageSendResult {

    @ApiModelProperty("消息ID")
    private String messageId;

    @ApiModelProperty("原因")
    private String reason;

    @ApiModelProperty("成功/失败")
    private boolean success;

    public static MessageSendResult success(String id) {
        return new MessageSendResult(id, null, true);
    }

    public static MessageSendResult success(String id, String reason){
        return new MessageSendResult(id, reason, true);
    }

    public static MessageSendResult error(String id, String reason) {
        return new MessageSendResult(id, reason, false);
    }
}
