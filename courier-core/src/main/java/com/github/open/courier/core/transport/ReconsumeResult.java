package com.github.open.courier.core.transport;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "再消费的结果")
public class ReconsumeResult {

    @ApiModelProperty("消费ID")
    private String id;

    @ApiModelProperty("原因")
    private String reason;

    @ApiModelProperty("成功/失败")
    private boolean success;

    public static ReconsumeResult success(String id, String reason) {
        return new ReconsumeResult(id, reason, true);
    }

    public static ReconsumeResult error(String id, String reason) {
        return new ReconsumeResult(id, reason, false);
    }
}
