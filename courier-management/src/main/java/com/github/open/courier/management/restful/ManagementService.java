package com.github.open.courier.management.restful;

import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.converter.ExceptionConverter;
import com.github.open.courier.core.converter.MessageQueryConditionConverter;
import com.github.open.courier.core.transport.*;
import com.github.open.courier.management.infrastructure.converter.ConsumeMessageConverter;
import com.github.open.courier.management.infrastructure.converter.SendMessageConverter;
import com.github.open.courier.management.infrastructure.feign.ProducerClient;
import com.github.open.courier.repository.mapper.ConsumeRecordMapper;
import com.github.open.courier.repository.mapper.MessageMapper;
import com.github.open.courier.repository.mapper.SendFailMessageMapper;
import com.github.open.courier.repository.mapper.SubscribeMapper;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Courier
 */
@Api(tags = "人工补偿服务")
@Slf4j
@RestController
@RequiredArgsConstructor
public class ManagementService {

    private final SendFailMessageMapper sendFailMessageMapper;
    private final ConsumeRecordMapper consumeRecordMapper;
    private final MessageMapper messageMapper;
    private final ProducerClient producerClient;
    private final RestTemplate restTemplate;
    private final SubscribeMapper subscribeMapper;

    @ApiOperation("再发送")
    @ApiImplicitParam(name = "messageIds", value = "消息ID", dataType = "String", allowMultiple = true, required = true, paramType = "body")
    @PostMapping(URLConstant.MANAGEMENT_RESEND)
    public List<MessageSendResult> resend(@RequestBody QueryOperationRequest operationRequest) {

        if (CollectionUtils.isEmpty(operationRequest.getIds())) {
            return Collections.emptyList();
        }

        List<MessageSendResult> results = Lists.newArrayListWithCapacity(operationRequest.getIds().size());

        // 查询出发送成功和发送失败的消息
        List<SendFailMessage> failMessages = sendFailMessageMapper.selectByMessageIds(operationRequest.getIds());

        // 基于时间分片 传参startTime和endTime
        List<DBMessage> dbMessages = messageMapper
                .listByMessageIds(MessageQueryConditionConverter.toMessageQueryCondition(operationRequest));

        Set<String> existedIds = Stream.concat(failMessages.stream().map(SendFailMessage::getMessageId),
                dbMessages.stream().map(DBMessage::getMessageId))
                .collect(Collectors.toSet());

        // 如果参数中有未知的id, 则这些未知的id发送失败
        if (existedIds.size() != operationRequest.getIds().size()) {
            List<MessageSendResult> notFoundIds = operationRequest.getIds().stream()
                    .filter(id -> !existedIds.contains(id))
                    .map(id -> MessageSendResult.error(id, "该ID查询不到消息"))
                    .collect(Collectors.toList());
            results.addAll(notFoundIds);
        }

        if (CollectionUtils.isEmpty(existedIds)) {
            return results;
        }

        // 转换为SendMessage
        List<SendMessage> sendMessages = SendMessageConverter.toSendMessage(failMessages, dbMessages);

        // 发送
        List<MessageSendResult> sendResults;
        try {
            sendResults = producerClient.send(sendMessages);
        } catch (Exception e) {
            log.error("发送消息失败, messageIds:{}", operationRequest.getIds(), e);

            String cause = "发送消息失败: " + e.getMessage();
            List<MessageSendResult> sendFailResults = sendMessages.stream()
                    .map(m -> MessageSendResult.error(m.getMessageId(), cause))
                    .collect(Collectors.toList());
            results.addAll(sendFailResults);
            return results;
        }

        if (CollectionUtils.isEmpty(sendResults)) {
            log.warn("发送消息返回为空, messageIds:{}", operationRequest.getIds());
            return results;
        }

        results.addAll(sendResults);

        // 发送成功的消息, 从失败消息表中删除
        List<String> successIds = sendResults.stream()
                .filter(MessageSendResult::isSuccess)
                .map(MessageSendResult::getMessageId)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(successIds)) {
            sendFailMessageMapper.deleteBatch(successIds);
        }

        return results;
    }

    @ApiOperation("再消费")
    @ApiImplicitParam(name = "ids", value = "消费ID", dataType = "String", allowMultiple = true, required = true, paramType = "body")
    @PostMapping(URLConstant.MANAGEMENT_RECONSUME)
    public List<ReconsumeResult> reconsume(@RequestBody QueryOperationRequest operationRequest) {

        if (CollectionUtils.isEmpty(operationRequest.getIds())) {
            return Collections.emptyList();
        }

        List<ReconsumeResult> results = Lists.newArrayListWithCapacity(operationRequest.getIds().size());
        // 基于时间分片 传参startTime和endTime
        List<ConsumeRecord> records = consumeRecordMapper
                .listByIds(MessageQueryConditionConverter.toMessageQueryCondition(operationRequest));

        if (CollectionUtils.isEmpty(records)) {
            return operationRequest.getIds().stream()
                    .map(id -> ReconsumeResult.error(id, "该ID找不到消费消息"))
                    .collect(Collectors.toList());
        }

        Map<String, List<ConsumeMessage>> urlConsumeMessages = new HashMap<>();
        records.stream().collect(Collectors.groupingBy(ConsumeRecord::getGroupId)).forEach(
                (groupId, crs) -> {
                    String cluster = subscribeMapper.whereCluster(groupId);
                    urlConsumeMessages.putAll(ConsumeMessageConverter.toConsumeMessagesByRecord(cluster, crs));
                }
        );

        long count = urlConsumeMessages.values().stream().mapToInt(List::size).count();

        // 对于查询不到的id, 当做失败
        if (count != operationRequest.getIds().size()) {
            List<String> existedIds = urlConsumeMessages.values().stream().flatMap(l -> l.stream().map(ConsumeMessage::getId)).collect(Collectors.toList());
            List<ReconsumeResult> notFoundIds = operationRequest.getIds().stream()
                    .filter(id -> !existedIds.contains(id))
                    // 该消息可能是由 messageBus 发出的。
                    .map(id -> ReconsumeResult.error(id, "该ID找不到消费消息"))
                    .collect(Collectors.toList());
            results.addAll(notFoundIds);
        }

        if (count == 0) {
            return results;
        }

        // 消费, 推送成功的消息, 这里不从fail表中删除, 因为这里只是推送成功, client端消费成功/失败后才会去操作fail表
        List<ReconsumeResult> consumeResults = urlConsumeMessages
                .entrySet()
                .stream()
                .map(en -> push(en.getKey(), en.getValue()))
                .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);

        results.addAll(consumeResults);
        return results;
    }


    private List<ReconsumeResult> push(String url, List<ConsumeMessage> messages) {

        List<String> messageIds = messages.stream().map(ConsumeMessage::getId).collect(Collectors.toList());

        log.info("推送 url: {}, messages: {}", url, messageIds);

        String cause = null;
        try {
            restTemplate.postForEntity(url, messages, Void.class);
        } catch (Exception e) {
            cause = ExceptionConverter.getCause(e);
        }

        if (cause == null) {
            return messages.stream().map(m -> ReconsumeResult.success(m.getId(), "推送成功")).collect(Collectors.toList());
        } else {
            log.warn("手动补发消息失败, {}", cause);
            String finalCause = cause;
            return messages.stream().map(m -> ReconsumeResult.error(m.getId(), finalCause)).collect(Collectors.toList());
        }
    }

}