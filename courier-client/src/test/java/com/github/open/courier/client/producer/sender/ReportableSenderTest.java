package com.github.open.courier.client.producer.sender;

import com.github.open.courier.client.feign.ManagementClient;
import com.github.open.courier.core.transport.MessageSendResult;
import com.github.open.courier.core.transport.SendFailMessage;
import com.github.open.courier.core.transport.SendMessage;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReportableSenderTest {

    private ReportableSender reportableSender;

    @Mock
    private ManagementClient managementClient;

    @Mock
    private Sender sender;

    @Before
    public void before() {
        Reportable reporter = new SimpleReporter();
        reportableSender = new ReportableSender(sender, reporter, managementClient, 500);
    }

    @Test
    public void trySend_when_send_success() {

        when(sender.trySend(any(SendMessage.class))).thenReturn(MessageSendResult.success("id123"));

        Assert.assertTrue(reportableSender.trySend(new SendMessage()).isSuccess());
    }

    @Test
    public void trySend_when_send_fail() {

        when(sender.trySend(any(SendMessage.class))).thenReturn(MessageSendResult.error("id123", "test"));
        doNothing().when(managementClient).sendFail(any(SendFailMessage.class));

        Assert.assertFalse(reportableSender.trySend(new SendMessage().setRetries(0)).isSuccess());
    }

    @Test
    public void trySendBatch_when_send_success() {

        List<SendMessage> messages = Lists.newArrayList();

        List<MessageSendResult> sendResults = Lists.newArrayList();
        for (int i = 1; i <= 10; i++) {

            messages.add(new SendMessage().setMessageId("id00" + i).setRetries(0).setContent("MessageContent" + i));
            sendResults.add(new MessageSendResult().setMessageId("id00" + i).setSuccess(true));
        }

        when(sender.trySend(messages)).thenReturn(sendResults);

        Assert.assertEquals(10, reportableSender.trySend(messages).size());
    }

    @Test
    public void trySendBatch_when_send_fail() {

        List<SendMessage> messages = Lists.newArrayList();

        List<MessageSendResult> sendResults = Lists.newArrayList();
        for (int i = 1; i <= 10; i++) {

            messages.add(new SendMessage().setMessageId("id00" + i).setRetries(0).setContent("MessageContent" + i));
            sendResults.add(new MessageSendResult().setMessageId("id00" + i).setSuccess(false));
        }

        when(sender.trySend(messages)).thenReturn(sendResults);

        Assert.assertEquals(10, reportableSender.trySend(messages).size());
    }

    @Test
    public void trySendBatch_when_send_partial_success() {

        List<SendMessage> messages = Lists.newArrayList();

        List<MessageSendResult> sendResults = Lists.newArrayList();
        for (int i = 1; i <= 10; i++) {

            messages.add(new SendMessage().setMessageId("id00" + i).setRetries(0).setContent("MessageContent" + i));
            if (i < 4) {
                sendResults.add(new MessageSendResult().setMessageId("id00" + i).setSuccess(false));
            } else {
                sendResults.add(new MessageSendResult().setMessageId("id00" + i).setSuccess(true));
            }
        }

        when(sender.trySend(messages)).thenReturn(sendResults);

        Assert.assertEquals(10, reportableSender.trySend(messages).size());
    }

}