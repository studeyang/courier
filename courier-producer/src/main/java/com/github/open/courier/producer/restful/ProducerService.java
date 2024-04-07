package com.github.open.courier.producer.restful;

import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.converter.MessageJsonConverter;
import com.github.open.courier.core.transport.SendMessage;
import com.github.open.courier.commons.redis.RedisHelper;
import com.github.open.courier.commons.support.CourierServerProperties;
import com.github.open.courier.core.converter.BackupMessageConverter;
import com.github.open.courier.core.converter.ExceptionConverter;
import com.github.open.courier.core.support.Wrapper;
import com.github.open.courier.core.transport.DBMessage;
import com.github.open.courier.core.transport.MessageSendResult;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.github.open.courier.core.constant.MessageConstant.*;
import static com.github.open.courier.core.transport.MessageSendResult.error;
import static com.github.open.courier.core.transport.MessageSendResult.success;

/**
 * @author Courier
 */
@Api(tags = "生产者服务")
@Slf4j
@RestController
public class ProducerService {

    /**
     * 网关超时时间: 5s, 这里保险起见用4.5s
     */
    private static final long GATEWAY_TIMEOUT = 4500;

    /**
     * 批量发送时, 每条消息最多的超时时间
     */
    private static final long MAX_TIMEOUT_PER_MESSAGE = 500;

    private static final String MESSAGE_ERROR = "消息校验不通过, message:{}, reason:{}";

    private final Producer<String, String> producer;
    private final RedisHelper redisHelper;
    private final CourierServerProperties.ProducerProperties properties;

    public ProducerService(Producer<String, String> producer,
                           RedisHelper redisHelper,
                           CourierServerProperties serverProperties) {
        this.producer = producer;
        this.redisHelper = redisHelper;
        this.properties = serverProperties.getProducer();
    }

    @ApiOperation("发送一条消息")
    @ApiImplicitParam(name = "message", value = "消息", dataType = "SendMessage", required = true, paramType = "body")
    @PostMapping(URLConstant.PRODUCER_SEND)
    public MessageSendResult send(@RequestBody SendMessage message) {

        log.debug("发送一条消息: {}", message);

        MessageSendResult errorResult = validate(message);

        if (errorResult != null) {
            log.error(MESSAGE_ERROR, message, errorResult.getReason());
            return errorResult;
        }

        ProducerRecord<String, String> producerRecord = creatRecord(message);
        RecordMetadata recordMetadata;
        try {
            recordMetadata = producer.send(producerRecord).get(GATEWAY_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            String cause = ExceptionConverter.getCause(e);
            log.error("发送kafka, message:{}, cause:{}", message, cause);
            Thread.currentThread().interrupt();
            return error(message.getMessageId(), cause);
        }

        pushRedis(message, recordMetadata);

        return success(message.getMessageId());
    }

    @ApiOperation("发送一批消息")
    @ApiImplicitParam(name = "messages", value = "消息", dataType = "SendMessage", required = true, allowMultiple = true, paramType = "body")
    @PostMapping(URLConstant.PRODUCER_SENDS)
    public List<MessageSendResult> send(@RequestBody List<SendMessage> messages) {

        log.debug("发送一批消息: {}", messages);

        if (CollectionUtils.isEmpty(messages)) {
            return Collections.emptyList();
        }

        long begin = System.currentTimeMillis();
        long deadline = begin + GATEWAY_TIMEOUT;

        List<MessageSendResult> results = Lists.newArrayListWithCapacity(messages.size());
        // 发送成功的消息才会记录入库, 消息发送失败会被不断重试
        Map<String, SendMessage> allMessages = Maps.newHashMapWithExpectedSize(messages.size());
        List<SendMessage> completeMessages = Lists.newArrayListWithCapacity(messages.size());
        List<SendMessage> successMessages = Lists.newArrayListWithCapacity(messages.size());
        Map<String, RecordMetadata> successRecordMetadatas = Maps.newHashMapWithExpectedSize(messages.size());
        Map<String, Future<RecordMetadata>> features = Maps.newHashMapWithExpectedSize(messages.size());

        // 校验消息的完整性，如果消息有缺陷什么也不处理，直接返回
        messages.forEach(message -> {

            MessageSendResult errorResult = validate(message);
            if (errorResult != null) {
                log.error(MESSAGE_ERROR, message, errorResult.getReason());
                results.add(errorResult);
                return;
            }
            String messageId = message.getMessageId();
            allMessages.put(messageId, message);
            completeMessages.add(message);
        });

        Set<String> failTopics = Sets.newHashSet();

        // 创建 topic 耗时较大，如果超过网关的连接时间，则全部返回 error，需要客户端重试（topic 同步到阿里云不支持批量）
        if (isGatewayTimeout(results, completeMessages, deadline)) {
            return results;
        }
        // 处理 topic 同步失败的情况，包含有这些 topic 的消息需要移除
        List<SendMessage> sendMessages = removeFailTopics(completeMessages, failTopics, results);

        doSend(sendMessages, features, results);

        long[] timeout = {deadline - System.currentTimeMillis()};
        // 然后每个feature都get, 时间逐步减少, 整个过程不会超过 4500ms (GATEWAY_TIMEOUT)
        features.forEach((messageId, feature) -> {

            long t = timeout[0];
            if (t <= 0) {
                results.add(error(messageId, "发送kafka消息超时"));
            } else {
                try {
                    RecordMetadata recordMetadata = feature.get(Math.min(t, MAX_TIMEOUT_PER_MESSAGE), TimeUnit.MILLISECONDS);
                    results.add(success(messageId));
                    successMessages.add(allMessages.get(messageId));
                    successRecordMetadatas.put(messageId, recordMetadata);
                } catch (Exception e) {
                    String cause = ExceptionConverter.getCause(e);
                    log.error("批量发送kafka失败超时, message:{}, cause:{}", allMessages.get(messageId), cause);
                    results.add(error(messageId, cause));
                    Thread.currentThread().interrupt();
                }
                timeout[0] = deadline - System.currentTimeMillis();
            }
        });

        if (CollectionUtils.isNotEmpty(successMessages)) {
            pushRedis(successMessages, successRecordMetadatas);
        }

        return results;
    }

    @PostMapping("/courier/redisBufferTest")
    public void redisBuffer(@RequestBody SendMessage message) {
        TopicPartition topicPartition = new TopicPartition("testRedisBuffer", 0);
        RecordMetadata recordMetadata = new RecordMetadata(topicPartition, -1, 0L,
                System.currentTimeMillis(), Long.valueOf(0), 0, 0);
        pushRedis(message, recordMetadata);
    }

    private List<SendMessage> removeFailTopics(List<SendMessage> completeMessages, Set<String> failTopics
            , List<MessageSendResult> results) {

        //不存在直接返回
        if (CollectionUtils.isEmpty(failTopics)) {
            return completeMessages;
        }
        List<SendMessage> result = Lists.newArrayListWithCapacity(completeMessages.size());
        completeMessages.forEach(message -> {
            if (failTopics.contains(message.getTopic())) {
                results.add(error(message.getMessageId(), "该 topic 同步到阿里云失败。"));
                return;
            }
            result.add(message);
        });
        return result;
    }

    private void doSend(List<SendMessage> completeMessages, Map<String, Future<RecordMetadata>> features, List<MessageSendResult> results) {

        // 每个消息先发送, 获取feature
        completeMessages.forEach(message -> {

            String messageId = message.getMessageId();
            ProducerRecord<String, String> producerRecord = creatRecord(message);
            try {
                features.put(messageId, producer.send(producerRecord));
            } catch (Exception e) {
                String cause = ExceptionConverter.getCause(e);
                log.error("批量发送kafka失败, message:{}, cause:{}", message, cause);
                results.add(error(messageId, cause));
            }
        });
    }

    /**
     * 判断是否超时，如果超时连接已被切断则全部返回失败。
     * 客户端调用该接口应该设置超时时间，超时之后应该重试，
     */
    private boolean isGatewayTimeout(List<MessageSendResult> results, List<SendMessage> completeMessages, long deadline) {

        if (deadline - System.currentTimeMillis() > 0) {
            return false;
        }
        String cause = "topic 同步到阿里云导致网关超时，消息需要重新发送。";
        completeMessages.forEach(message -> results.add(error(message.getMessageId(), cause)));
        return true;
    }

    /**
     * 推送成功消息到redis
     */
    private void pushRedis(SendMessage message, RecordMetadata recordMetadata) {
        DBMessage dbMessage = BackupMessageConverter.toDBMessage(message, recordMetadata);
        try {
            redisHelper.leftPush(KAFKA_BACKUP_MESSAGES, MessageJsonConverter.toJson(Collections.singletonList(dbMessage)));
        } catch (Exception e) {
            log.error("发送redis失败, message:{}", message, e);
        }
    }

    /**
     * 推送成功消息到redis
     */
    private void pushRedis(List<SendMessage> messages, Map<String, RecordMetadata> recordMetadatas) {

        if (CollectionUtils.isEmpty(messages)) {
            return;
        }

        List<DBMessage> dbMessages = BackupMessageConverter.toDBMessages(messages, recordMetadatas);
        try {
            redisHelper.leftPush(KAFKA_BACKUP_MESSAGES,
                    MessageJsonConverter.toJson(dbMessages));
        } catch (Exception e) {
            // 20210802：MessageJsonConverter.toJson(dbMessages)出现过oom
            log.error("批量发送redis失败, messages:{}", messages, e);
        }
    }

    /**
     * 创建record
     */
    private ProducerRecord<String, String> creatRecord(SendMessage message) {
        List<Header> headers = new ArrayList<>();
        headers.add(new RecordHeader(CLUSTER, message.getCluster().getBytes()));
        headers.add(new RecordHeader(ENV, message.getEnv().getBytes()));
        return new ProducerRecord<>(
                message.getTopic(),
                null,
                message.getCreatedAt().getTime(),
                message.getKey(),
                message.getContent(),
                headers);
    }

    /**
     * 校验完整性
     */
    private MessageSendResult validate(SendMessage message) {
        if (message == null) {
            return error(null, "消息不能为空");
        }
        if (StringUtils.isBlank(message.getMessageId())) {
            return error(null, "messageId不能为空");
        }
        if (StringUtils.isBlank(message.getTopic())) {
            return error(message.getMessageId(), "topic不能为空");
        }
        if (!Wrapper.isWrappedTopic(message.getTopic())) {
            return error(message.getMessageId(), "topic ' " + message.getTopic() + " '必须以[" + Wrapper.getTopicPrefix() + "]开头");
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

        int messageMaxLength = properties.getMessageMaxLength();
        int messageLength = StringUtils.length(message.getContent());
        if (messageMaxLength != -1 && messageLength > messageMaxLength) {
            return error(message.getMessageId(), String.format("message长度(%s)不可超过%s", messageLength, messageMaxLength));
        }

        if (message.getCreatedAt() == null) {
            return error(message.getMessageId(), "createdAt不能为空");
        }
        if (message.getUsage() == null) {
            return error(message.getMessageId(), "usage不能为空");
        }
        return null;
    }

}
