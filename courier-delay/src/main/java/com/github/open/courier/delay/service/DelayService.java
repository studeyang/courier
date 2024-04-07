package com.github.open.courier.delay.service;

import com.github.open.courier.commons.support.CourierServerProperties;
import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.converter.DelayMessageConverter;
import com.github.open.courier.core.support.Wrapper;
import com.github.open.courier.core.transport.DelayMessage;
import com.github.open.courier.core.transport.MessageSendResult;
import com.github.open.courier.core.transport.SendMessage;
import com.github.open.courier.delay.biz.DelayMessageBizService;
import com.github.open.courier.delay.biz.MessageBizService;
import com.github.open.courier.delay.core.ScheduleManager;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.github.open.courier.core.transport.MessageSendResult.error;


@Api(tags = "延迟消息服务")
@Slf4j
@RestController
@RequiredArgsConstructor
public class DelayService {

    private static final String MESSAGE_ERROR = "消息校验不通过, message:{}, reason:{}";

    private static final long MIN_DELAY_SEC = 1L;
    private static final long MAX_DELAY_DAY = 30L;

    private final DelayMessageBizService delayMessageBizService;
    private final MessageBizService messageBizService;
    private final ScheduleManager scheduleManager;
    private final CourierServerProperties properties;

    @ApiOperation("发送一条延迟消息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "message", value = "消息", dataType = "SendMessage", required = true, paramType = "body")
    })
    @PostMapping(URLConstant.DELAY_SEND)
    public MessageSendResult send(@RequestBody SendMessage message) {

        MessageSendResult errorResult = validate(message);

        if (Objects.nonNull(errorResult)) {
            log.error(MESSAGE_ERROR, message, errorResult.getReason());
            return errorResult;
        }

        long expireTime = calculateExpireTime(message);

        if (isExpireTimeOut(expireTime)) {
            return handleTimeOutMessage(message, expireTime);
        }

        if (scheduleManager.canPush(expireTime)) {

            return handleInsidePreReadRangeMessage(message, expireTime);

        } else {
            return handleOutsidePreReadRangeMessage(message, expireTime);
        }
    }


    @ApiOperation("发送一批延迟消息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "messages", value = "消息", dataType = "SendMessage", required = true, allowMultiple = true, paramType = "body")
    })
    @PostMapping(URLConstant.DELAY_SENDS)
    public List<MessageSendResult> send(@RequestBody List<SendMessage> messages) {

        if (CollectionUtils.isEmpty(messages)) {
            return Collections.emptyList();
        }

        List<MessageSendResult> messageSendResults = Lists.newArrayList();
        List<SendMessage> validatePassMessages = Lists.newArrayList();

        messages.forEach(message -> {

            MessageSendResult validateResult = validate(message);

            if (Objects.nonNull(validateResult)) {
                log.error(MESSAGE_ERROR, message, validateResult.getReason());
                messageSendResults.add(validateResult);
            } else {
                validatePassMessages.add(message);
            }
        });

        if (CollectionUtils.isEmpty(validatePassMessages)) {
            return messageSendResults;
        }

        //计算到期时间值,获取第一个消息的创建时间来计算过期时间，同一批消息创建时间相同
        long expireTime = calculateExpireTime(validatePassMessages.get(0));

        if (isExpireTimeOut(expireTime)) {

            List<MessageSendResult> timeOutHandleResult = handleTimeOutMessage(validatePassMessages, expireTime);

            messageSendResults.addAll(timeOutHandleResult);

            return messageSendResults;
        }

        if (scheduleManager.canPush(expireTime)) {

            List<MessageSendResult> insideHandleResult = handleInsidePreReadRangeMessage(validatePassMessages, expireTime);

            messageSendResults.addAll(insideHandleResult);

            return messageSendResults;

        } else {

            List<MessageSendResult> outsideHandleResult = handleOutsidePreReadRangeMessage(validatePassMessages, expireTime);

            messageSendResults.addAll(outsideHandleResult);

            return messageSendResults;
        }
    }


    private MessageSendResult handleTimeOutMessage(SendMessage message, long expireTime) {

        Date startDeliveTime = new Date();

        MessageSendResult messageSendResult = messageBizService.send(message);

        Date endDeliveTime = new Date();

        if (messageSendResult.isSuccess()) {

            DelayMessage delayMessage = DelayMessageConverter.toDelayMessage(message, expireTime,
                    Boolean.TRUE, Boolean.TRUE, startDeliveTime, endDeliveTime);

            delayMessageBizService.insert(delayMessage);
        }

        return messageSendResult;
    }


    private List<MessageSendResult> handleTimeOutMessage(List<SendMessage> messages, long expireTime) {

        Date startDeliveTime = new Date();

        List<MessageSendResult> messageSendResults = messageBizService.send(messages);

        Date endDeliveTime = new Date();

        Map<Boolean, List<MessageSendResult>> resultPartition = messageSendResults.stream()
                .collect(Collectors.partitioningBy(MessageSendResult::isSuccess));

        List<MessageSendResult> successPartition = resultPartition.get(Boolean.TRUE);

        // 发送成功的消息存入数据库
        if (CollectionUtils.isNotEmpty(successPartition)) {

            List<String> successMessageIds = successPartition.stream()
                    .map(MessageSendResult::getMessageId).collect(Collectors.toList());

            List<SendMessage> successMessages = messages.stream()
                    .filter(item -> successMessageIds.contains(item.getMessageId())).collect(Collectors.toList());

            List<DelayMessage> delayMessages = DelayMessageConverter.toDelayMessages(successMessages, expireTime,
                    Boolean.TRUE, Boolean.TRUE, startDeliveTime, endDeliveTime);

            delayMessageBizService.insertList(delayMessages);
        }

        return messageSendResults;
    }


    private MessageSendResult handleInsidePreReadRangeMessage(SendMessage message, long expireTime) {

        DelayMessage delayMessage = DelayMessageConverter.toDelayMessage(
                message, expireTime, Boolean.TRUE, Boolean.FALSE, null, null);

        return delayMessageBizService.handleInsidePreReadRange(delayMessage);
    }


    /**
     * 这里的 inside 指的是时间轮里的消息
     */
    private List<MessageSendResult> handleInsidePreReadRangeMessage(List<SendMessage> messages, long expireTime) {

        List<DelayMessage> delayMessages = DelayMessageConverter.toDelayMessages(
                messages, expireTime, Boolean.TRUE, Boolean.FALSE, null, null);

        return delayMessageBizService.handleInsidePreReadRange(delayMessages, expireTime);
    }

    /**
     * 这里的 outside 指的是时间轮外的消息
     */
    private MessageSendResult handleOutsidePreReadRangeMessage(SendMessage message, long expireTime) {

        DelayMessage delayMessage = DelayMessageConverter.toDelayMessage(
                message, expireTime, Boolean.FALSE, Boolean.FALSE, null, null);

        if (!delayMessageBizService.insert(delayMessage)) {
            return MessageSendResult.error(message.getMessageId(), "延迟消息存入数据库失败");
        }

        return MessageSendResult.success(message.getMessageId());
    }


    private List<MessageSendResult> handleOutsidePreReadRangeMessage(List<SendMessage> messages, long expireTime) {

        List<DelayMessage> delayMessages = DelayMessageConverter.toDelayMessages(
                messages, expireTime, Boolean.FALSE, Boolean.FALSE, null, null);

        boolean result = delayMessageBizService.insertList(delayMessages);

        return result ? toSuccess(messages) : toFail(messages, "延迟消息批量存入数据库失败");
    }


    private long calculateExpireTime(SendMessage message) {

        return message.getCreatedAt().getTime() + message.getDelayMillis();
    }


    private boolean isExpireTimeOut(long expireTime) {

        return expireTime - System.currentTimeMillis() <= 0;
    }


    public List<MessageSendResult> toSuccess(List<SendMessage> sendMessages) {

        return sendMessages.stream()
                .map(o -> MessageSendResult.success(o.getMessageId())).collect(Collectors.toList());
    }


    public List<MessageSendResult> toFail(List<SendMessage> sendMessages, String failReason) {

        return sendMessages.stream()
                .map(o -> MessageSendResult.error(o.getMessageId(), failReason)).collect(Collectors.toList());
    }

    private MessageSendResult validate(SendMessage message) {
        if (Objects.isNull(message)) {
            return error(null, "消息不能为空");
        }
        if (StringUtils.isBlank(message.getMessageId())) {
            return error(null, "messageId不能为空");
        }
        if (StringUtils.isBlank(message.getTopic())) {
            return error(message.getMessageId(), "topic不能为空");
        }
        if (!Wrapper.isWrappedTopic(message.getTopic())) {
            return error(message.getMessageId(), "topic必须以[" + Wrapper.getTopicPrefix() + "]开头");
        }
        if (StringUtils.isBlank(message.getType())) {
            return error(message.getMessageId(), "type不能为空");
        }
        if (StringUtils.isBlank(message.getService())) {
            return error(message.getMessageId(), "service不能为空");
        }
        if (StringUtils.isBlank(message.getContent())) {
            return error(message.getMessageId(), "content不能为空");
        }

        String clusterAndEnvValidateMsg = String.format(
                "订阅服务来源未知 cluster=%s, env=%s, 请升级 courier-spring-boot-starter v2.0 及以上",
                message.getCluster(),
                message.getEnv());
        if (StringUtils.isBlank(message.getCluster()) || StringUtils.isBlank(message.getEnv())) {
            return error(message.getMessageId(), clusterAndEnvValidateMsg);
        }

        int messageMaxLength = properties.getProducer().getMessageMaxLength();
        int messageLength = StringUtils.length(message.getContent());
        if (messageMaxLength != -1 && messageLength > messageMaxLength) {
            return error(message.getMessageId(), String.format("message长度(%s)不可超过%s", messageLength, messageMaxLength));
        }

        if (Objects.isNull(message.getCreatedAt())) {
            return error(message.getMessageId(), "createdAt不能为空");
        }
        if (Objects.isNull(message.getUsage())) {
            return error(message.getMessageId(), "usage不能为空");
        }
        if (Objects.isNull(message.getDelayMillis())) {
            return error(message.getMessageId(), "delayTime不能为空");
        }
        if (message.getDelayMillis() < TimeUnit.SECONDS.toMillis(MIN_DELAY_SEC)) {
            return error(message.getMessageId(), "延迟时间不能小于1秒");
        }
        if (message.getDelayMillis() > TimeUnit.DAYS.toMillis(MAX_DELAY_DAY)) {
            return error(message.getMessageId(), "延迟时间不能大于30天");
        }
        return null;
    }


}
