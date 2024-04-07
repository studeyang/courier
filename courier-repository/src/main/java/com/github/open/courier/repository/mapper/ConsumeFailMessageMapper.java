package com.github.open.courier.repository.mapper;

import com.github.open.courier.core.transport.ConsumeFailMessage;
import com.github.open.courier.core.transport.QueryConsumeMessageRequest;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * 消费失败消息Mapper
 */
@Mapper
@Repository
public interface ConsumeFailMessageMapper {

    void deleteBatch(List<String> ids);

    List<ConsumeFailMessage> listRepush(int size);

    int insertBatch(List<ConsumeFailMessage> failMessages);

    void updateNeedRepushByIds(Collection<String> consumeIds);

    List<ConsumeFailMessage> query(QueryConsumeMessageRequest request);

    int countQuery(QueryConsumeMessageRequest request);

    List<ConsumeFailMessage> queryConsumeRecord(QueryConsumeMessageRequest request);
}
