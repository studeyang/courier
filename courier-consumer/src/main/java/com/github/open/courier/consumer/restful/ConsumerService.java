package com.github.open.courier.consumer.restful;

import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.listener.AbstractConsumerListener;
import com.github.open.courier.core.transport.ConsumeMessage;
import com.github.open.courier.core.transport.SendMessage;
import com.github.open.courier.repository.mapper.ConsumeRecordMapper;
import com.github.open.courier.core.listener.AbstractListenerContainer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.open.courier.core.constant.MessageConstant.CLUSTER;
import static com.github.open.courier.core.constant.MessageConstant.ENV;
import static org.apache.kafka.clients.consumer.ConsumerRecord.NO_TIMESTAMP;
import static org.apache.kafka.clients.consumer.ConsumerRecord.NULL_SIZE;

/**
 * @author Courier
 */
@Api(tags = "消费服务")
@Slf4j
@RestController
public class ConsumerService {

    @Autowired
    private ConsumeRecordMapper consumeRecordMapper;
    @Autowired
    private AbstractListenerContainer listenerContainer;

    @ApiOperation("ConsumeMessage入库")
    @ApiImplicitParam(name = "messages", value = "消息入库", dataType = "ConsumeMessage", paramType = "body")
    @PostMapping(URLConstant.CONSUMER_RECORD)
    public void record(@RequestBody List<ConsumeMessage> messages) {
        if (CollectionUtils.isNotEmpty(messages)) {
            consumeRecordMapper.insertBatchAddPushTime(messages);
        }
    }

    @ApiOperation("广播记录入库")
    @ApiImplicitParam(name = "messages", value = "广播记录入库", dataType = "ConsumeMessage", paramType = "body")
    @PostMapping(URLConstant.CONSUMER_INSERT)
    public void insertBatch(@RequestBody List<ConsumeMessage> messages) {
        if (CollectionUtils.isNotEmpty(messages)) {
            consumeRecordMapper.insertBatch(messages);
        }
    }

    @PostMapping("/courier/messageTransferTest")
    public void messageTransferTest(@RequestParam String listenerName, @RequestBody SendMessage message) {
        AbstractConsumerListener listener = listenerContainer.getListenerMap().get(listenerName);
        if (listener == null) {
            log.warn("listener [{}] 未找到。", listenerName);
            return;
        }
        listener.handle(getConsumerRecords(message));
    }

    private ConsumerRecords<String, String> getConsumerRecords(SendMessage message) {
        Map<TopicPartition, List<ConsumerRecord<String, String>>> records = new HashMap<>();
        Headers headers = new RecordHeaders();
        headers.add(new RecordHeader(CLUSTER, message.getCluster().getBytes()));
        headers.add(new RecordHeader(ENV, message.getEnv().getBytes()));
        records.put(
                new TopicPartition(message.getTopic(), 0),
                Collections.singletonList(
                        new ConsumerRecord<>(
                                message.getTopic(),
                                0,
                                4190,
                                NO_TIMESTAMP,
                                TimestampType.NO_TIMESTAMP_TYPE,
                                -1L,
                                NULL_SIZE,
                                NULL_SIZE,
                                message.getMessageId(),
                                message.getContent(),
                                headers)
                )
        );

        return new ConsumerRecords<>(records);
    }

}
