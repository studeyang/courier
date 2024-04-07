package com.github.open.courier.management.restful;


import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.transport.*;
import com.github.open.courier.repository.mapper.ConsumeFailMessageMapper;
import com.github.open.courier.repository.mapper.ConsumeRecordMapper;
import com.github.open.courier.repository.mapper.MessageMapper;
import com.github.open.courier.repository.mapper.SendFailMessageMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author LIJIAHAO
 * 消息分页查询
 */

@RestController
@RequiredArgsConstructor
public class MessageQueryService {

    private final MessageMapper messageMapper;
    private final ConsumeRecordMapper recordMapper;
    private final ConsumeFailMessageMapper consumeFailMapper;
    private final SendFailMessageMapper sendFailMapper;

    @PostMapping(URLConstant.MANAGEMENT_QUERY_PAGE_SEND_SUCCESS)
    public MessagePageDTO querySendSuccess(@RequestBody QuerySendMessageRequest request) {

        PageHelper.startPage(request.getStart(), request.getLength());

        List<DBMessage> data = messageMapper.queryAll(request);

        return wrapReturnData(data);

    }

    @PostMapping(URLConstant.MANAGEMENT_QUERY_PAGE_SEND_FAIL)
    public MessagePageDTO querySendFail(@RequestBody QuerySendMessageRequest request) {

        PageHelper.startPage(request.getStart(), request.getLength());

        List<ConsumeFailMessage> data = sendFailMapper.queryAll(request);

        return wrapReturnData(data);
    }

    @PostMapping(URLConstant.MANAGEMENT_QUERY_PAGE_CONSUME_SUCCESS)
    public MessagePageDTO queryConsumeSuccess(@RequestBody QueryConsumeMessageRequest request) {

        PageHelper.startPage(request.getStart(), request.getLength());

        List<ConsumeRecord> data = recordMapper.queryConsumeRecord(request);

        return wrapReturnData(data);
    }

    @PostMapping(URLConstant.MANAGEMENT_QUERY_PAGE_CONSUME_FAIL)
    public MessagePageDTO queryConsumeFail(@RequestBody QueryConsumeMessageRequest request) {

        PageHelper.startPage(request.getStart(), request.getLength());

        List<ConsumeFailMessage> data = consumeFailMapper.queryConsumeRecord(request);

        return wrapReturnData(data);
    }

    /**
     * 结果封装成页面数据和分页数据
     *
     * @param data
     * @return
     */
    private MessagePageDTO wrapReturnData(List<?> data) {

        PageInfo<?> pageInfo = new PageInfo<>(data);

        return new MessagePageDTO(data, pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize());
    }
}
