package com.github.open.courier.example.controller;

import com.github.open.courier.core.message.Broadcast;
import com.github.open.courier.eventing.BroadcastPublisher;
import com.github.open.courier.eventing.EventPublisher;
import com.github.open.courier.example.event.ExampleBroadcast;
import com.github.open.courier.example.event.ExampleEvent;
import com.github.open.courier.messaging.Event;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Courier
 */
@Slf4j
@RestController
public class ExampleMessagePublishController {

    @GetMapping("/event/{content}/{sleep}")
    public void publishEvent(@PathVariable String content, @PathVariable long sleep) {

        Event event = createEvent(content, sleep);

        EventPublisher.publish(event);
    }

    @GetMapping("/event/batch/{content}/{sleep}/{size}")
    public void publishEventBatch(@PathVariable String content, @PathVariable long sleep, @PathVariable int size) {

        List<Event> events = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Event event = createEvent(content + i, sleep);
            events.add(event);
        }
        EventPublisher.publish(events);
    }

    @GetMapping("/event/delay/{content}/{sleep}/{delayTime}/{timeUnit}")
    public void publishEventDelay(@PathVariable String content,
                                  @PathVariable long sleep,
                                  @PathVariable Long delayTime,
                                  @PathVariable String timeUnit) {

        Event event = createEvent(content, sleep);

        EventPublisher.publish(event, delayTime, toTimeUnit(timeUnit));

    }

    @GetMapping("/event/delay/batch/{content}/{sleep}/{size}/{delayTime}/{timeUnit}")
    public void publishEventDelayBatch(@PathVariable String content,
                                       @PathVariable long sleep,
                                       @PathVariable int size,
                                       @PathVariable Long delayTime,
                                       @PathVariable String timeUnit) {

        List<Event> events = Collections.nCopies(size, createEvent(content, sleep));

        EventPublisher.publish(events, delayTime, toTimeUnit(timeUnit));
    }

    @Transactional
    @GetMapping("/event/transaction/{content}/{sleep}")
    public void publishEventTransaction(@PathVariable String content, @PathVariable long sleep) {

        Event event = createEvent(content, sleep);

        EventPublisher.publishTransaction(event);
    }

    @Transactional
    @GetMapping("/event/transaction/batch/{content}/{sleep}/{size}")
    public void publishEventTransactionBatch(@PathVariable String content, @PathVariable long sleep, @PathVariable int size) {

        List<Event> events = Collections.nCopies(size, createEvent(content, sleep));

        EventPublisher.publishTransaction(events);
    }

    @GetMapping("/broadcast/{content}/{sleep}")
    public void publishBroadcast(@PathVariable String content, @PathVariable long sleep) {

        Broadcast broadcast = createBroadcast(content, sleep);

        BroadcastPublisher.publish(broadcast);
    }

    @GetMapping("/broadcast/batch/{content}/{sleep}/{size}")
    public void publishBroadcastBatch(@PathVariable String content, @PathVariable long sleep, @PathVariable int size) {

        List<Broadcast> broadcasts = Collections.nCopies(size, createBroadcast(content, sleep));

        BroadcastPublisher.publish(broadcasts);
    }

    @Transactional
    @GetMapping("/broadcast/transaction/{content}/{sleep}")
    public void publishBroadcastTransaction(@PathVariable String content, @PathVariable long sleep) {

        Broadcast broadcast = createBroadcast(content, sleep);

        BroadcastPublisher.publishTransaction(broadcast);
    }

    @Transactional
    @GetMapping("/broadcast/transaction/batch/{content}/{sleep}/{size}")
    public void publishBroadcastTransactionBatch(@PathVariable String content, @PathVariable long sleep, @PathVariable int size) {

        List<Broadcast> broadcasts = Collections.nCopies(size, createBroadcast(content, sleep));

        BroadcastPublisher.publishTransaction(broadcasts);
    }

    // ------------------------------------------------------------------------------------------------------

    Event createEvent(String content, long sleep) {
        ExampleEvent event = new ExampleEvent();
        event.setContent(content);
        event.setSleep(sleep);
        return event;
    }

    Broadcast createBroadcast(String content, long sleep) {
        ExampleBroadcast broadcast = new ExampleBroadcast();
        broadcast.setContent(content);
        broadcast.setSleep(sleep);
        return broadcast;
    }

    TimeUnit toTimeUnit(String unit) {
        TimeUnit timeUnit = TimeUnit.SECONDS;
        if (StringUtils.equals("s", unit)) {
            timeUnit = TimeUnit.SECONDS;
        }
        if (StringUtils.equals("m", unit)) {
            timeUnit = TimeUnit.MINUTES;
        }
        if (StringUtils.equals("h", unit)) {
            timeUnit = TimeUnit.HOURS;
        }
        if (StringUtils.equals("d", unit)) {
            timeUnit = TimeUnit.DAYS;
        }
        return timeUnit;
    }

}
