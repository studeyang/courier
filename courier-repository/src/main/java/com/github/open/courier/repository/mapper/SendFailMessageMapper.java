package com.github.open.courier.repository.mapper;

import com.github.open.courier.core.transport.ConsumeFailMessage;
import com.github.open.courier.core.transport.QuerySendMessageRequest;
import com.github.open.courier.core.transport.SendFailMessage;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * 发送失败消息Mapper
 */
@Mapper
@Repository
public interface SendFailMessageMapper {

    void insert(SendFailMessage message);

    void insertBatch(Collection<SendFailMessage> messages);

    void deleteBatch(Collection<String> ids);

    List<SendFailMessage> selectByMessageIds(Collection<String> messageIds);

    List<ConsumeFailMessage> query(QuerySendMessageRequest request);

    List<ConsumeFailMessage> queryAll(QuerySendMessageRequest request);

    int countQuery(QuerySendMessageRequest request);
}
