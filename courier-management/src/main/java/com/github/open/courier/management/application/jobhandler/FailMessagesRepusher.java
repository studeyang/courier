package com.github.open.courier.management.application.jobhandler;

import com.github.open.courier.repository.mapper.ConsumeFailMessageMapper;
import com.github.open.courier.repository.mapper.SubscribeMapper;
import com.github.open.courier.core.transport.ConsumeFailMessage;
import com.github.open.courier.management.infrastructure.converter.ConsumeMessageConverter;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 定时得再消费数据库中已经消费失败的消息
 */
@Slf4j
@Component
@JobHandler("FailMessagesRepusher")
public class FailMessagesRepusher extends AbstractRepusher {

    @Autowired
    private ConsumeFailMessageMapper consumeFailMapper;
    @Autowired
    private SubscribeMapper subscribeMapper;

    @Override
    public ReturnT<String> execute(String args) {

        List<ConsumeFailMessage> failMessages = consumeFailMapper.listRepush(50);

        if (CollectionUtils.isEmpty(failMessages)) {
            return ReturnT.SUCCESS;
        }

        Set<String> ids = failMessages.stream().map(ConsumeFailMessage::getId).collect(Collectors.toSet());

        log.info("开始推送失败消息(ConsumeFailMessage), cid:{}", ids);

        // 查询出的这批fail消息, 无论重试结果如何, 不再继续重试
        consumeFailMapper.updateNeedRepushByIds(ids);

        // 查询db表, 组装成ConsumeMessage, 并推送重试
        failMessages.stream().collect(Collectors.groupingBy(ConsumeFailMessage::getGroupId)).forEach(
                (groupId, consumeFails) -> {
                    String cluster = subscribeMapper.whereCluster(groupId);
                    ConsumeMessageConverter.toConsumeMessagesByFail(cluster, consumeFails)
                            .forEach(this::push);
                }
        );

        return ReturnT.SUCCESS;
    }
}
