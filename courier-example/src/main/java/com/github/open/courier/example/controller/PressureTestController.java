package com.github.open.courier.example.controller;

import com.github.open.courier.eventing.EventPublisher;
import com.github.open.courier.example.event.ExampleEvent;
import com.github.open.courier.messaging.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Courier
 */
@Slf4j
@RestController
public class PressureTestController {

    @GetMapping("/pressure/{size}")
    public void pressure(@PathVariable int size) {

        List<Event> events = IntStream.range(0, size)
                .parallel()
                .mapToObj(i -> createEvent(createRandomContent(), 0))
                .collect(Collectors.toList());

        EventPublisher.publish(events);
    }

    static final char[] chars = "0123456789abcdefghijklmnopqrstuvwxyz ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    static String createRandomContent() {

        ThreadLocalRandom r = ThreadLocalRandom.current();

        int length = r.nextInt(1000) + 500;

        char[] buff = new char[length];
        for (int i = 0; i < length; i++) {
            buff[i] = chars[r.nextInt(chars.length)];
        }
        return new String(buff);
    }

    Event createEvent(String content, long sleep) {
        ExampleEvent event = new ExampleEvent();
        event.setContent(content);
        event.setSleep(sleep);
        return event;
    }
}
