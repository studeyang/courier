package com.github.open.courier.example.eventhandler;

import com.github.open.courier.annotation.EventHandler;
import com.github.open.courier.example.event.ExampleBroadcast;
import com.github.open.courier.example.event.ExampleEvent;
import com.github.open.courier.messaging.Message;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Courier
 */
@Slf4j
@Component
@EventHandler(topic = "test", consumerGroup = "example")
public class ExampleMessageEventHandler {

    public void handle(ExampleEvent event) {
        log(event, event.getSleep());
        if (event.isException()) {
            throw new TestException(event.getExceptionMessage());
        }
    }

//    public void handle(ExampleEvent2 event) {
//        log(event, event.getSleep());
//        if (event.isException()) {
//            throw new TestException(event.getExceptionMessage());
//        }
//    }

    public void handle(ExampleBroadcast broadcast) {
        log(broadcast, broadcast.getSleep());
    }

    @SneakyThrows
    void log(Message m, long sleep) {
//        if (log.isInfoEnabled()) {
//            log.info("----- kafka消费中 ---> {} : {}", m.getClass().getSimpleName(), m);
//        }
        if (m instanceof ExampleEvent) {
            long now = System.currentTimeMillis();
            long delay = now - ((ExampleEvent) m).getNow().getTime();
            log.warn("----- kafka消费中 ---> delay : {}", delay);
        }
        Thread.sleep(sleep);
    }

    private static class TestException extends RuntimeException {
        public TestException(String message) {
            super(message);
        }
    }

}
