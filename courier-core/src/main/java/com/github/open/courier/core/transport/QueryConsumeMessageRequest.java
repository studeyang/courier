package com.github.open.courier.core.transport;

import java.util.Date;

import lombok.Data;

@Data
public class QueryConsumeMessageRequest {

    private String consumeId;

    private String messageId;

    private String fromService;

    private String toService;

    private Date startTime;

    private Date endTime;

    private int start;

    private int length;
}
