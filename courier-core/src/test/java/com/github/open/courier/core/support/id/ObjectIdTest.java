package com.github.open.courier.core.support.id;

import org.junit.Test;
import org.springframework.util.StopWatch;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.stream.IntStream;

public class ObjectIdTest {

    @Test
    public void test_threadId() {
        System.out.print(Thread.currentThread().getName() + " ");
        System.out.println(Thread.currentThread().getId());
        new Thread(() -> {
            System.out.print(Thread.currentThread().getName() + " ");
            System.out.println(Thread.currentThread().getId());
        }).start();
    }

    @Test
    public void test_objectId() throws UnknownHostException {
        System.out.println(InetAddress.getLocalHost().getHostAddress());

        System.out.println(Thread.currentThread().getId());
        System.out.println(new ObjectId());

        new Thread(() -> {
            System.out.println(Thread.currentThread().getId());
            System.out.println(new ObjectId());
        }).start();

        new Thread(() -> {
            System.out.println(Thread.currentThread().getId());
            System.out.println(new ObjectId());
        }).start();
    }

    @Test
    public void performance() {
        StopWatch stopWatch = new StopWatch();
        int count = 1000_0000;
        new ObjectId();
        new ObjectIdOld();

        stopWatch.start("old");
        IntStream.range(0, count).forEach(i ->
                ObjectIdOld.getId()
        );
        stopWatch.stop();

        stopWatch.start("new");
        IntStream.range(0, count).forEach(i ->
                ObjectId.getId()
        );
        stopWatch.stop();

        System.out.println(stopWatch.prettyPrint());
    }

}