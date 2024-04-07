package com.github.open.courier.management.application.jobhandler;

import com.github.open.courier.core.vo.UpdateConsumeRecord;
import com.github.open.courier.repository.mapper.ConsumeRecordMapper;
import com.github.open.courier.repository.mapper.SubscribeMapper;
import com.github.open.courier.core.transport.ConsumeRecord;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.open.courier.management.infrastructure.converter.ConsumeMessageConverter.toConsumeMessagesByRecord;

/**
 * 定时得再消费数据库中已经超时的消息记录
 * <p>
 * 30s/次
 */
@Slf4j
@Component
@JobHandler("TimeOutMessagesRepusher")
public class TimeOutMessagesRepusher extends AbstractRepusher {

    @Autowired
    private ConsumeRecordMapper consumeRecordMapper;
    @Autowired
    private SubscribeMapper subscribeMapper;

    @Override
    public ReturnT<String> execute(String args) {

        // 前1小时到前3分钟之间, 还未消费完, 且没有重试过的消息
        LocalDateTime begin = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().minusMinutes(3);
        List<ConsumeRecord> records = consumeRecordMapper.listTimeOut(begin, end, 50);

        if (CollectionUtils.isEmpty(records)) {
            return ReturnT.SUCCESS;
        }

        List<String> ids = records.stream().map(ConsumeRecord::getId).collect(Collectors.toList());

        log.info("开始推送超时消息(ConsumeRecord), cid:{}", ids);

        // 查询出的这批timeout消息, 无论重试推送结果如何, 不再继续扫描
        UpdateConsumeRecord consumeRecord = new UpdateConsumeRecord()
                .setNeedRepush(false)
                .setIds(ids)
                .setPollTimeBegin(toDate(begin))
                .setPollTimeEnd(toDate(end));
        consumeRecordMapper.updateByIdsSelective(consumeRecord);

        // 查询db表, 组装成ConsumeMessage, 并重试推送
        records.stream().collect(Collectors.groupingBy(ConsumeRecord::getGroupId)).forEach(
                (groupId, crs) -> {
                    String cluster = subscribeMapper.whereCluster(groupId);
                    toConsumeMessagesByRecord(cluster, crs).forEach(this::push);
                }
        );

        return ReturnT.SUCCESS;
    }

    private Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}

