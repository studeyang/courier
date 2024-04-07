package com.github.open.courier.repository.sharding;

import com.github.open.courier.core.constant.MessageConstant;
import com.github.open.courier.core.constant.Separator;
import com.github.open.courier.core.utils.DateUtils;
import com.google.common.collect.Range;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingValue;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author Courier
 */
@Component("messageRecordShardingAlgorithm")
public class MessageRecordShardingAlgorithm implements ComplexKeysShardingAlgorithm<Date> {

    @Override
    public Collection<String> doSharding(Collection<String> collection,
                                         ComplexKeysShardingValue<Date> complexKeysShardingValue) {

        Assert.notNull(complexKeysShardingValue, "complexKeysShardingValue must not be null");

        List<String> list = new ArrayList<>();
        // 分片键的值，这里是操作 mapper 中解析出来的值
        List<Date> createTimes = (List<Date>) complexKeysShardingValue.getColumnNameAndShardingValuesMap()
                .get(MessageConstant.MESSAGE_RECORD_SHARDING_KEY);
        Range<Date> range = complexKeysShardingValue.getColumnNameAndRangeValuesMap()
                .get(MessageConstant.MESSAGE_RECORD_SHARDING_KEY);

        //查询时范围分片
        if (Objects.nonNull(range)) {

            Date startDate;
            Date endDate;
            if (range.lowerEndpoint() instanceof Date) {
                startDate = range.lowerEndpoint();
                endDate = range.upperEndpoint();
            } else {
                Comparable lower = range.lowerEndpoint();
                Comparable upper = range.upperEndpoint();
                startDate = DateUtils.localDateTimeToDate((LocalDateTime) lower);
                endDate = DateUtils.localDateTimeToDate((LocalDateTime) upper);
            }

            List<String> dateList = DateUtils.getDatesByDatePeriod(startDate, endDate);
            if (CollectionUtils.isNotEmpty(dateList)) {
                dateList.forEach(item -> {
                    String actualPrefix = complexKeysShardingValue.getLogicTableName() + Separator.UNDERLINE.getSymbol() + item;
                    list.add(actualPrefix);
                });
            }
        }
        //插入时唯一分片
        if (CollectionUtils.isNotEmpty(createTimes)) {

            String tableSuffix = complexKeysShardingValue.getLogicTableName() + Separator.UNDERLINE.getSymbol()
                    + DateUtils.formatDate(createTimes.get(0), DateUtils.yyyyMMdd);
            list.add(tableSuffix);
        }
        return list;
    }
}