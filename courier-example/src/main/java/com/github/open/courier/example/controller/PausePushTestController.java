package com.github.open.courier.example.controller;

import com.github.open.courier.eventing.EventPublisher;
import com.github.open.courier.example.event.ExampleEvent;
import com.github.open.courier.messaging.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yanglulu
 */
@Slf4j
@RestController
public class PausePushTestController {

    @GetMapping("/pausepush/keepproducing/{size}/{sleep}/{lastingSecond}")
    public void publishEventBatch(@PathVariable Integer size,
                                  @PathVariable Long sleep,
                                  @PathVariable Integer lastingSecond) throws InterruptedException {
        List<Event> events = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ExampleEvent event = new ExampleEvent();
            event.setContent("KeepProducingMessage" + (i + 1));
            event.setSleep(sleep);
            events.add(event);
        }
        long startTime = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - startTime < lastingSecond * 1000) {
                EventPublisher.publish(events);
            } else {
                break;
            }
            Thread.sleep(100);
        }
    }

}
