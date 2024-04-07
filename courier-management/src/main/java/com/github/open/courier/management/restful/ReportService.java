package com.github.open.courier.management.restful;

import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.transport.ConsumeState;
import com.github.open.courier.core.transport.MessageConsumeTime;
import com.github.open.courier.core.transport.SendFailMessage;
import com.github.open.courier.core.utils.DateUtils;
import com.github.open.courier.core.vo.UpdateConsumeRecord;
import com.github.open.courier.repository.mapper.ConsumeFailMessageMapper;
import com.github.open.courier.repository.mapper.ConsumeRecordMapper;
import com.github.open.courier.repository.mapper.SendFailMessageMapper;
import com.github.open.courier.core.transport.ConsumeFailMessage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Api(tags = "消息报告服务")
@RestController
@Log4j2
public class ReportService {

    @Resource
    private SendFailMessageMapper sendFailMessageMapper;
    @Resource
    private ConsumeRecordMapper consumeRecordMapper;
    @Resource
    private ConsumeFailMessageMapper consumeFailMessageMapper;

    @ApiOperation("报告一条消息发送失败")
    @ApiImplicitParam(name = "message", value = "发送失败消息", dataType = "SendFailMessage", required = true, paramType = "body")
    @PostMapping(URLConstant.MANAGEMENT_SEND_FAIL)
    public void sendFail(@RequestBody SendFailMessage message) {
        sendFailMessageMapper.insert(message);
    }

    @ApiOperation("报告一批消息发送失败")
    @ApiImplicitParam(name = "messages", value = "发送失败消息", dataType = "SendFailMessage", allowMultiple = true, required = true, paramType = "body")
    @PostMapping(URLConstant.MANAGEMENT_SEND_FAILS)
    public void sendFail(@RequestBody List<SendFailMessage> messages) {
        sendFailMessageMapper.insertBatch(messages);
    }

    @ApiOperation("报告消费成功")
    @ApiImplicitParam(name = "consumeTime", value = "消费成功时间", dataType = "MessageConsumeTime", allowMultiple = true, required = true, paramType = "body")
    @PostMapping(URLConstant.MANAGEMENT_HANDLE_SUCCESS)
    public void handleSuccess(@RequestBody List<MessageConsumeTime> consumeTime) {

        // 作客户端兼容
        for (MessageConsumeTime messageConsumeTime : consumeTime) {

            if (Objects.isNull(messageConsumeTime.getPollStartTime())) {
                Date yesterday = org.apache.commons.lang3.time.DateUtils.addDays(new Date(), -1);
                messageConsumeTime.setPollStartTime(DateUtils.parseDate(yesterday, "00:00:00"));
            }

            if (Objects.isNull(messageConsumeTime.getPollEndTime())) {
                messageConsumeTime.setPollEndTime(DateUtils.parseDate(new Date(), "23:59:59"));
            }
        }

        if (CollectionUtils.isNotEmpty(consumeTime)) {
            consumeFailMessageMapper.deleteBatch(consumeTime.stream().map(MessageConsumeTime::getId).collect(Collectors.toList()));
        }

        consumeTime.forEach(item -> {
            try {
                consumeRecordMapper.updateStateAndClientTimeByIds(item);
            } catch (Exception e) {
                log.warn("更新 courier_consume_record 表失败，id: {}, pollStartTime: {}, pollEndTime: {}", item.getId(),
                        item.getPollStartTime(), item.getPollEndTime(), e);
            }
        });
    }

    @ApiOperation("报告消费失败")
    @ApiImplicitParam(name = "messages", value = "消费失败消息", dataType = "ConsumeFailMessage", allowMultiple = true, required = true, paramType = "body")
    @PostMapping(URLConstant.MANAGEMENT_HANDLE_FAIL)
    public void handleFail(@RequestBody List<ConsumeFailMessage> messages) {

        if (CollectionUtils.isEmpty(messages)) {
            return;
        }

        consumeFailMessageMapper.insertBatch(messages);

        List<Date> dates = messages.stream().map(ConsumeFailMessage::getPollTime).collect(Collectors.toList());

        // 作客户端兼容
        Date now = new Date();
        Date yesterday = org.apache.commons.lang3.time.DateUtils.addDays(now, -1);

        List<String> ids = messages.stream().map(ConsumeFailMessage::getId).collect(Collectors.toList());
        Date startTime = Collections.min(dates) == null ? DateUtils.parseDate(yesterday, "00:00:00") : Collections.min(dates);
        Date endTime = Collections.max(dates) == null ? now : Collections.max(dates);

        try {
            UpdateConsumeRecord update = new UpdateConsumeRecord()
                    .setNeedRepush(false)
                    .setState(ConsumeState.HANDLE_FAIL)
                    .setIds(ids)
                    .setPollTimeBegin(startTime)
                    .setPollTimeEnd(endTime);

            consumeRecordMapper.updateByIdsSelective(update);
        } catch (Exception e) {
            log.warn("更新 courier_consume_record 表失败，ids: {}, startTime: {}, endTime: {}", ids, startTime, endTime, e);
        }
    }


}
