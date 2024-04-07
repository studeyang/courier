package com.github.open.courier.repository.mapper;

import com.github.open.courier.core.transport.DBMessage;
import com.github.open.courier.core.transport.MessageQueryCondition;
import com.github.open.courier.core.transport.QuerySendMessageRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 全量消息Mapper
 */
@Mapper
@Repository
public interface MessageMapper {

    // 直接插入message
    void insertBatch(List<DBMessage> messages);

    List<DBMessage> listByMessageIds(@Param("queryCondition") MessageQueryCondition queryCondition);

    List<DBMessage> query(QuerySendMessageRequest request);

    List<DBMessage> queryAll(QuerySendMessageRequest request);

    int countQuery(QuerySendMessageRequest request);

    List<String> listTableNames(@Param("tableName") String tableName);

    void createTable(@Param("tableName") String tableName);

    void dropTable(@Param("tableName") String tableName);
}
