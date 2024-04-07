package com.github.open.courier.example.event;

import com.github.open.courier.annotation.Topic;
import com.github.open.courier.messaging.Event;
import lombok.Data;

import java.util.Date;

/**
 * @author Courier
 */
@Data
@Topic(name = "test")
public class ExampleEvent2 extends Event {

    // @PrimaryKey

    private String content;

    private long sleep;

    private boolean exception;

    private String exceptionMessage;

    private Date now = new Date();
}
