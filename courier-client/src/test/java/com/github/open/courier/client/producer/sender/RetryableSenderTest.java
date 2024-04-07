package com.github.open.courier.client.producer.sender;

import com.github.open.courier.core.transport.MessageSendResult;
import com.github.open.courier.core.transport.SendMessage;
import org.apache.commons.collections4.ListUtils;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RetryableSenderTest {

    private RetryableSender retryableSender;

    @Mock
    private Sender sender;

    @Before
    public void before() {
        retryableSender = new RetryableSender(sender);
    }

    @Test
    public void trySend_when_send_success() {

        MessageSendResult sendResult = MessageSendResult.success("id123");
        when(sender.trySend(any(SendMessage.class))).thenReturn(sendResult);

        Assert.assertTrue(retryableSender.trySend(new SendMessage()).isSuccess());
    }

    @Test
    public void trySend_when_send_fail() {

        MessageSendResult sendResult = MessageSendResult.error("id123", "test");
        when(sender.trySend(any(SendMessage.class))).thenReturn(sendResult);

        retryableSender.trySend(new SendMessage());

        verify(sender, times(4)).trySend(new SendMessage());
    }

    /**
     * 模拟重试成功的场景： 重试一次后成功
     */
    @Test
    public void trySend_when_retry_success() {

        MessageSendResult sendFail = MessageSendResult.error("id123", "test");
        MessageSendResult retrySuccess = MessageSendResult.success("id123");

        SendMessage expectFailMessage = new SendMessage().setMessageId("id123").setRetries(0);

        when(sender.trySend(expectFailMessage)).thenAnswer((Answer<MessageSendResult>) invocation -> {

            Integer retries = (Integer) invocation.getMethod().getParameterTypes()[0]
                    .getMethod("getRetries")
                    .invoke(expectFailMessage);

            if (retries == 0) {

                invocation.getMethod().getParameterTypes()[0]
                        .getMethod("setRetries", Integer.class)
                        .invoke(expectFailMessage, retries + 1);
                return sendFail;
            }

            return retrySuccess;
        });

        retryableSender.trySend(expectFailMessage);

        verify(sender, times(2)).trySend(expectFailMessage);
    }

    /**
     * 场景：发送成功
     */
    @Test
    public void trySendBatch_when_send_success() {

        List<SendMessage> messages = Lists.newArrayList();
        List<MessageSendResult> sendResults = Lists.newArrayList();
        for (int i = 1; i <= 10; i++) {

            messages.add(new SendMessage().setMessageId("id00" + i));
            sendResults.add(new MessageSendResult().setMessageId("id00" + i).setSuccess(true));
        }

        when(sender.trySend(anyList())).thenReturn(sendResults);

        retryableSender.trySend(messages);

        verify(sender, times(1)).trySend(messages);
    }

    /**
     * 场景：部分发送成功，重试失败
     */
    @Test
    public void trySendBatch_when_partial_send_success_and_retry_fail() {

        List<SendMessage> messages = Lists.newArrayList();

        List<MessageSendResult> sendResults = Lists.newArrayList();
        for (int i = 1; i <= 10; i++) {

            messages.add(new SendMessage().setMessageId("id00" + i).setRetries(0));

            if (i < 4) {
                sendResults.add(new MessageSendResult().setMessageId("id00" + i).setSuccess(false));
            } else {
                sendResults.add(new MessageSendResult().setMessageId("id00" + i).setSuccess(true));
            }
        }

        when(sender.trySend(messages)).thenReturn(sendResults);

        retryableSender.trySend(messages);

        //正常发送了 1 次
        verify(sender, times(1)).trySend(messages);
        // id001, id002, id003 这三条消息重试了 3 次
        verify(sender, times(3)).trySend(ListUtils.partition(messages, 3).get(0));
    }

    /**
     * 场景：部分发送成功，部分重试成功
     */
    @Test
    public void trySendBatch_when_partial_send_success_and_partial_retry_success() {

        Set<String> idWillSendFail = Sets.newSet("id001", "id002", "id003");

        List<SendMessage> messages = Lists.newArrayList();
        List<MessageSendResult> sendResults = Lists.newArrayList();
        for (int i = 1; i <= 10; i++) {

            messages.add(new SendMessage().setMessageId("id00" + i).setRetries(0));

            if (i < 4) {
                sendResults.add(new MessageSendResult().setMessageId("id00" + i).setSuccess(false));
            } else {
                sendResults.add(new MessageSendResult().setMessageId("id00" + i).setSuccess(true));
            }
        }

        when(sender.trySend(messages)).thenAnswer((Answer<List<MessageSendResult>>) invocation -> {

            SendMessage sendMessage = (SendMessage) invocation.getMethod().getParameterTypes()[0]
                    .getMethod("get", int.class)
                    .invoke(messages, 0);

            if (idWillSendFail.contains(sendMessage.getMessageId())) {

                sendMessage.setMessageId("id001-");
                sendResults.get(0).setMessageId("id001-").setSuccess(true);
            }

            return sendResults;
        });

        Assert.assertEquals(8, retryableSender.trySend(messages).stream().filter(MessageSendResult::isSuccess).count());
        //正常发送了 1 次
        verify(sender, times(1)).trySend(messages);
    }

}