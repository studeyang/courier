package com.github.open.courier.management.application.service;

import com.github.open.courier.core.constant.Separator;
import com.github.open.courier.repository.constant.TableConstant;
import com.github.open.courier.repository.mapper.ConsumeRecordMapper;
import com.github.open.courier.repository.mapper.DelayMessageMapper;
import com.github.open.courier.repository.mapper.MessageMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * 对分表操作的业务
 *
 * @author yanglulu
 */
@Service
public class ShardingTableAppService {

    @Resource
    private MessageMapper messageMapper;
    @Resource
    private ConsumeRecordMapper consumeRecordMapper;
    @Resource
    private DelayMessageMapper delayMessageMapper;

    /**
     * 创建分表
     */
    public void createShardingTable() {
        createShardingTable(TableConstant.COURIER_MESSAGE, 1, 2,
                sharding -> messageMapper.createTable(sharding));

        createShardingTable(TableConstant.COURIER_CONSUME_RECORD, 1, 2,
                sharding -> consumeRecordMapper.createTable(sharding));

        createShardingTable(TableConstant.COURIER_DELAY_MESSAGE, 1, 31,
                sharding -> delayMessageMapper.createTable(sharding));
    }

    /**
     * 创建 todayBefore - todayAfter 范围内的分表
     *
     * @param tableName   表名
     * @param todayBefore 前 todayBefore 天
     * @param todayAfter  后 todayAfter 天
     * @param creator     创建者
     */
    public void createShardingTable(String tableName, int todayBefore, int todayAfter, Consumer<String> creator) {

        SimpleDateFormat sdf = new SimpleDateFormat(com.github.open.courier.core.utils.DateUtils.yyyyMMdd);

        Date today = new Date();

        IntStream.rangeClosed(-todayBefore, todayAfter).forEach(
                i -> creator.accept(tableName + Separator.UNDERLINE.getSymbol() + sdf.format(DateUtils.addDays(today, i)))
        );
    }


    /**
     * 删除分表
     */
    public void dropShardingTable() {

        dropShardingTable(TableConstant.COURIER_MESSAGE, 30,
                tableName -> messageMapper.listTableNames(tableName),
                tableName -> messageMapper.dropTable(tableName));

        dropShardingTable(TableConstant.COURIER_CONSUME_RECORD, 30,
                tableName -> messageMapper.listTableNames(tableName),
                tableName -> messageMapper.dropTable(tableName));

        dropShardingTable(TableConstant.COURIER_DELAY_MESSAGE, 30,
                tableName -> messageMapper.listTableNames(tableName),
                tableName -> messageMapper.dropTable(tableName));
    }


    /**
     * 删除 todayBefore 之前的表
     *
     * @param originTableName 原表名
     * @param todayBefore     前todayBefore天
     * @param seletor         查表者
     * @param deletor         删表者
     */
    public void dropShardingTable(String originTableName,
                                  int todayBefore,
                                  Function<String, List<String>> seletor,
                                  Consumer<String> deletor) {


        SimpleDateFormat sdf = new SimpleDateFormat(com.github.open.courier.core.utils.DateUtils.yyyyMMdd);

        Date today = new Date();

        String compareTable = originTableName + Separator.UNDERLINE.getSymbol() + sdf.format(DateUtils.addDays(today, -todayBefore));

        List<String> tableNames = seletor.apply(originTableName);

        if (CollectionUtils.isEmpty(tableNames)) {
            return;
        }

        for (String tableName : tableNames) {
            if (StringUtils.equals(originTableName, tableName) || tableName.compareTo(compareTable) > 0) {
                continue;
            }
            deletor.accept(tableName);
        }
    }


}
