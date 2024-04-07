package com.github.open.courier.example.event;

import com.github.open.courier.annotation.Topic;
import com.github.open.courier.core.message.Broadcast;
import lombok.Data;

import java.util.Date;

/**
 * @author Courier
 */
@Data
@Topic(name = "test")
public class ExampleBroadcast extends Broadcast {

    private String content;

    private long sleep;

    private Date now = new Date();
}
