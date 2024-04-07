package com.github.open.courier.core.listener;

import org.apache.kafka.clients.consumer.Consumer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ListenerSwitchTest {

    @InjectMocks
    private ListenerSwitch listenerSwitch;

    @Mock
    private Consumer<String, String> consumer;

    @Test
    public void pauseConsume() {

        Mockito.doNothing().when(consumer).pause(Mockito.any());

        listenerSwitch.pauseConsume(consumer, "PUSH", "courier-example", "");
        Assert.assertTrue(listenerSwitch.getKafkaPaused());

        listenerSwitch.pauseConsume(consumer, "PUSH", "courier-example", "");
        Assert.assertTrue(listenerSwitch.getKafkaPaused());

        listenerSwitch.resumeConsume(consumer, "PUSH", "courier-example", "");
        Assert.assertFalse(listenerSwitch.getKafkaPaused());

        listenerSwitch.pauseConsume(consumer, "PUSH", "courier-example", "");
        Assert.assertTrue(listenerSwitch.getKafkaPaused());
    }

}