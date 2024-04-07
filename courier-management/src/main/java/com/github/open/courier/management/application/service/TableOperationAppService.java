package com.github.open.courier.management.application.service;

import com.github.open.courier.core.transport.TableOperationRequest;
import com.github.open.courier.repository.constant.TableConstant;
import com.github.open.courier.repository.mapper.ConsumeRecordMapper;
import com.github.open.courier.repository.mapper.DelayMessageMapper;
import com.github.open.courier.repository.mapper.MessageMapper;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.function.Consumer;

/**
 * 表创建/删除操作
 *
 * @author yanglulu
 */
@Service
@RequiredArgsConstructor
public class TableOperationAppService implements InitializingBean {

    private final MessageMapper messageMapper;
    private final ConsumeRecordMapper consumeRecordMapper;
    private final DelayMessageMapper delayMessageMapper;

    /**
     * key: tableName
     * value: the create operation of table
     */
    private Map<String, Consumer<String>> createOperationMap;

    /**
     * key: tableName
     * value: the drop operation of table
     */
    private Map<String, Consumer<String>> dropOperationMap;

    private static final String UNSUPPORT = "不支持操作";
    private static final String SUCCESS = "success";

    /**
     * 创建分表
     */
    public String createTable(TableOperationRequest request) {

        Consumer<String> operation = createOperationMap.get(request.getTable());

        Assert.notNull(operation, UNSUPPORT + request.getTable());

        operateTable(request, operation);

        return SUCCESS;
    }

    /**
     * 删除分表
     */
    public String dropTable(TableOperationRequest request) {

        Consumer<String> operation = dropOperationMap.get(request.getTable());

        Assert.notNull(operation, UNSUPPORT + request.getTable());

        operateTable(request, operation);

        return SUCCESS;
    }


    private void operateTable(TableOperationRequest request, Consumer<String> operation) {

        switch (request.getTable()) {

            case TableConstant.COURIER_MESSAGE:
                request.getSuffixList().forEach(
                        suffix -> operation.accept(TableConstant.COURIER_MESSAGE + suffix)
                );
                break;

            case TableConstant.COURIER_CONSUME_RECORD:
                request.getSuffixList().forEach(
                        suffix -> operation.accept(TableConstant.COURIER_CONSUME_RECORD + suffix)
                );
                break;

            case TableConstant.COURIER_DELAY_MESSAGE:
                request.getSuffixList().forEach(
                        suffix -> operation.accept(TableConstant.COURIER_DELAY_MESSAGE + suffix)
                );
                break;

            default:
                throw new UnsupportedOperationException(UNSUPPORT + request.getTable());
        }
    }

    @Override
    public void afterPropertiesSet() {
        createOperationMap = Maps.newHashMap();
        createOperationMap.put(TableConstant.COURIER_MESSAGE, messageMapper::createTable);
        createOperationMap.put(TableConstant.COURIER_CONSUME_RECORD, consumeRecordMapper::createTable);
        createOperationMap.put(TableConstant.COURIER_DELAY_MESSAGE, delayMessageMapper::createTable);

        dropOperationMap = Maps.newHashMap();
        dropOperationMap.put(TableConstant.COURIER_MESSAGE, messageMapper::dropTable);
        dropOperationMap.put(TableConstant.COURIER_CONSUME_RECORD, messageMapper::dropTable);
        dropOperationMap.put(TableConstant.COURIER_DELAY_MESSAGE, messageMapper::dropTable);
    }
}
