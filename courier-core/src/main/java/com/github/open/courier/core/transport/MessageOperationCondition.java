package com.github.open.courier.core.transport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageOperationCondition {

    private List<String> messageIds;

    private Date startDeliveTime;

    private Date endDeliveTime;

    private Long startTime;

    private Long endTime;

}
