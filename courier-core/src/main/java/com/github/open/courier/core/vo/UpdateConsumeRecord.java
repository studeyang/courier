package com.github.open.courier.core.vo;

import com.github.open.courier.core.transport.ConsumeState;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * @author yanglulu
 * @date 2021/8/11
 */
@Data
@Accessors(chain = true)
public class UpdateConsumeRecord {

    // 可更新字段

    private Integer retries;
    private ConsumeState state;
    private Boolean needRepush;
    private Date beforePushTime;
    private Date endPushTime;
    private Date clientReceiveTime;
    private Date clientEndTime;
    private Long clientHandledCost;

    // 供检索使用

    private Date pollTimeBegin;
    private Date pollTimeEnd;
    private List<String> ids;

}
