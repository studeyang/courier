package com.github.open.courier.core.converter;

import com.github.open.courier.core.transport.SendMessage;
import com.github.open.courier.core.transport.DelayMessage;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 延迟消息转换器
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DelayMessageConverter {

    public static SendMessage toSendMessage(DelayMessage delayMessage) {
        SendMessage message = new SendMessage();
        message.setUsage(delayMessage.getUsage());
        message.setCreatedAt(new Date());
        message.setService(delayMessage.getFromService());
        message.setContent(delayMessage.getContent());
        message.setType(delayMessage.getType());
        message.setTopic(delayMessage.getTopic());
        message.setMessageId(delayMessage.getMessageId());
        message.setPrimaryKey(delayMessage.getPrimaryKey());
        message.setRetries(delayMessage.getRetries());
        message.setCluster(delayMessage.getCluster());
        message.setEnv(delayMessage.getEnv());
        return message;
    }


    public static List<SendMessage> toSendMessages(List<DelayMessage> delayMessages) {

        if (CollectionUtils.isEmpty(delayMessages)) {
            return Collections.emptyList();
        }

        Date createAt = new Date();

        List<SendMessage> sendMessages = Lists.newArrayListWithCapacity(delayMessages.size());

        for (DelayMessage delayMessage : delayMessages) {
            SendMessage message = new SendMessage();
            message.setUsage(delayMessage.getUsage());
            message.setCreatedAt(createAt);
            message.setService(delayMessage.getFromService());
            message.setContent(delayMessage.getContent());
            message.setType(delayMessage.getType());
            message.setTopic(delayMessage.getTopic());
            message.setMessageId(delayMessage.getMessageId());
            message.setPrimaryKey(delayMessage.getPrimaryKey());
            message.setRetries(delayMessage.getRetries());
            message.setCluster(delayMessage.getCluster());
            message.setEnv(delayMessage.getEnv());
            sendMessages.add(message);
        }

        return sendMessages;
    }


    public static DelayMessage toDelayMessage(SendMessage sendmessage,
                                              Long expireTime,
                                              Boolean isPreRead,
                                              Boolean isSend,
                                              Date startDeliveTime,
                                              Date endDeliveTime) {
        if (sendmessage == null) {
            return null;
        }
        return new DelayMessage()
                .setContent(sendmessage.getContent())
                .setCreatedAt(sendmessage.getCreatedAt())
                .setMessageId(sendmessage.getMessageId())
                .setPrimaryKey(sendmessage.getPrimaryKey())
                .setRetries(sendmessage.getRetries())
                .setFromService(sendmessage.getService())
                .setTopic(sendmessage.getTopic())
                .setType(sendmessage.getType())
                .setUsage(sendmessage.getUsage())
                .setExpireTime(expireTime)
                .setIsPreread(isPreRead)
                .setIsSend(isSend)
                .setStartDeliveTime(startDeliveTime)
                .setEndDeliveTime(endDeliveTime)
                // 添加环境标识
                .setCluster(sendmessage.getCluster())
                .setEnv(sendmessage.getEnv());
    }


    public static List<DelayMessage> toDelayMessages(List<SendMessage> sendmessages,
                                                     Long expireTime,
                                                     Boolean isPreRead,
                                                     Boolean isSend,
                                                     Date startDeliveTime,
                                                     Date endDeliveTime) {

        if (CollectionUtils.isEmpty(sendmessages)) {
            return Collections.emptyList();
        }

        return sendmessages.stream()
                .map(message -> toDelayMessage(
                        message,
                        expireTime,
                        isPreRead,
                        isSend,
                        startDeliveTime,
                        endDeliveTime
                        )
                )
                .collect(Collectors.toList());
    }

}
