package com.github.open.courier.client.producer.converter;

import com.github.open.courier.core.converter.SendMessageConverter;
import com.github.open.courier.core.transport.MessageSendResult;
import com.github.open.courier.core.transport.SendMessage;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class SendMessageConverterTest {

    @Test
    public void classify() {

        List<SendMessage> messages = Lists.newArrayList();
        List<MessageSendResult> sendResults = Lists.newArrayList();

        for (int i = 1; i <= 10; i++) {

            messages.add(new SendMessage().setMessageId("id00" + i));

            if (i < 4) {
                sendResults.add(new MessageSendResult().setMessageId("id00" + i).setSuccess(false));
            } else {
                sendResults.add(new MessageSendResult().setMessageId("id00" + i).setSuccess(true));
            }
        }

        Map<Boolean, List<SendMessage>> messagesMap = SendMessageConverter.classify(messages, sendResults);

        Assert.assertEquals(7, messagesMap.get(true).size());
        Assert.assertEquals(3, messagesMap.get(false).size());
    }

}