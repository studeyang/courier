package com.github.open.courier.repository.mapper;

import com.github.open.courier.core.transport.DelayMessage;
import com.github.open.courier.core.transport.MessageOperationCondition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chengyan
 * @date 2020/12/3 9:33
 */
@Mapper
@Repository
public interface DelayMessageMapper {

    void insert(DelayMessage message);

    void insertList(List<DelayMessage> messages);

    List<DelayMessage> listNeedPreReadByExpireTimeRange(@Param("startTime") long startTime,
                                                        @Param("endTime") long endTime);

    void updateReadedByIds(@Param("operationCondition") MessageOperationCondition operationCondition);

    List<DelayMessage> listNeedSendByMessageIds(@Param("operationCondition") MessageOperationCondition operationCondition);

    void updateSendedByIds(@Param("operationCondition") MessageOperationCondition operationCondition);

    void createTable(@Param("tableName") String tableName);

}
