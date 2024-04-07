package com.github.open.courier.core.transport;

import java.util.Date;

import lombok.Data;

@Data
public class QuerySendMessageRequest {

    private String messageId;

    private String event;

    private String content;

    private String fromService;

    private Date startTime;

    private Date endTime;

    private int start;

    private int length;
}
