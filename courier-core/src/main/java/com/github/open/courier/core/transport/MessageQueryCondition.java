package com.github.open.courier.core.transport;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 2 * @Author: chengyan
 * 3 * @Date: 2020/9/17 17:26
 */
@Data
public class MessageQueryCondition {


    private List<String> messageIds;

    private Date startTime;

    private Date endTime;

}
