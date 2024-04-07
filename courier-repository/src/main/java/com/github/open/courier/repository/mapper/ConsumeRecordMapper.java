package com.github.open.courier.repository.mapper;

import com.github.open.courier.core.transport.*;
import com.github.open.courier.core.vo.UpdateConsumeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消费记录Mapper
 *
 * @author Courier
 */
@Mapper
@Repository
public interface ConsumeRecordMapper {

    /**
     * 直接插入 record
     *
     * @param messages 消息集合
     */
    void insertBatch(List<ConsumeMessage> messages);

    /**
     * 需要分表，只能添加方法
     *
     * @param messages 消息集合
     */
    void insertBatchAddPushTime(@Param("messages") List<ConsumeMessage> messages);

    /**
     * 直接更新 record
     *
     * @param consumeTime 消费时间
     */
    void updateStateAndClientTimeByIds(MessageConsumeTime consumeTime);

    int updateByIdsSelective(UpdateConsumeRecord updateConsumeRecord);

    /**
     * 直接查询 record
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @param size  大小
     * @return 消费记录
     */
    List<ConsumeRecord> listTimeOut(@Param("begin") LocalDateTime begin,
                                    @Param("end") LocalDateTime end,
                                    @Param("size") int size);

    List<ConsumeRecord> query(QueryConsumeMessageRequest request);

    int countQuery(QueryConsumeMessageRequest request);

    List<ConsumeRecord> listByIds(@Param("queryCondition") MessageQueryCondition queryCondition);


    void createTable(@Param("tableName") String tableName);

    List<ConsumeRecord> queryConsumeRecord(QueryConsumeMessageRequest request);

}
