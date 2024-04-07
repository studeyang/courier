package com.github.open.courier.client.producer.sender;

import com.github.open.courier.client.feign.ProducerClient;
import com.github.open.courier.core.transport.MessageSendResult;
import com.github.open.courier.core.transport.SendMessage;
import org.apache.commons.collections4.ListUtils;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ProducerSenderTest {

    //    @InjectMocks
    private ProducerSender producerSender;

    @Mock
    private ProducerClient producerClient;

    @Before
    public void before() {
        producerSender = new ProducerSender(producerClient, 500);
    }

    @Test
    public void trySend_when_send_success() {

        MessageSendResult messageSendResult = MessageSendResult.success("123");
        when(producerClient.send(any(SendMessage.class))).thenReturn(messageSendResult);

        Assert.assertTrue(producerSender.trySend(new SendMessage()).isSuccess());
    }

    @Test
    public void trySendBatch_when_less_than_partition() {

        List<MessageSendResult> sendResults = Lists.newArrayList();
        for (int i = 0; i < 400; i++) {

            sendResults.add(MessageSendResult.success("id-" + i + 1));
        }
        when(producerClient.send(anyList())).thenReturn(sendResults);

        List<SendMessage> sendMessages = Lists.newArrayList();
        for (int i = 0; i < 400; i++) {

            sendMessages.add(new SendMessage());
        }

        producerSender.trySend(sendMessages);

        verify(producerClient, times(1)).send(sendMessages);
    }

    @Test
    public void trySendBatch_when_greater_than_partition() {

        List<MessageSendResult> sendResults = Lists.newArrayList();
        for (int i = 0; i < 600; i++) {

            sendResults.add(MessageSendResult.success("id-" + i + 1));
        }
        when(producerClient.send(anyList())).thenReturn(sendResults);

        List<SendMessage> sendMessages = Lists.newArrayList();
        for (int i = 0; i < 600; i++) {

            sendMessages.add(new SendMessage());
        }

        List<SendMessage> partition1 = ListUtils.partition(sendMessages, 500).get(0);
        List<SendMessage> partition2 = ListUtils.partition(sendMessages, 500).get(1);

        producerSender.trySend(sendMessages);

        verify(producerClient, times(1)).send(partition1);
        verify(producerClient, times(1)).send(partition2);
    }

}
