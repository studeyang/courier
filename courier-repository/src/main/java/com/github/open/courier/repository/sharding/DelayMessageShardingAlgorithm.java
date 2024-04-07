package com.github.open.courier.repository.sharding;

import com.github.open.courier.core.constant.MessageConstant;
import com.github.open.courier.core.constant.Separator;
import com.github.open.courier.core.utils.DateUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingValue;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author yanglulu
 */
@Component("delayMessageShardingAlgorithm")
public class DelayMessageShardingAlgorithm implements ComplexKeysShardingAlgorithm<Long> {

    @Override
    public Collection<String> doSharding(Collection<String> collection,
                                         ComplexKeysShardingValue<Long> complexKeysShardingValue) {

        Assert.notNull(complexKeysShardingValue, "complexKeysShardingValue must not be null");

        List<String> list = Lists.newArrayList();

        // 分片键的值，这里是操作 mapper 中解析出来的值
        List<Long> expireTimes = (List<Long>) complexKeysShardingValue.getColumnNameAndShardingValuesMap()
                .get(MessageConstant.DELAY_MESSAGE_SHARDING_KEY);
        Range<Long> range = complexKeysShardingValue.getColumnNameAndRangeValuesMap()
                .get(MessageConstant.DELAY_MESSAGE_SHARDING_KEY);

        //查询时范围分片
        if (Objects.nonNull(range)) {
            Date startDate = new Date(range.lowerEndpoint());
            Date endDate = new Date(range.upperEndpoint());
            List<String> dateList = DateUtils.getDatesByDatePeriod(startDate, endDate);
            if (CollectionUtils.isNotEmpty(dateList)) {
                dateList.forEach(item -> {
                    String actualTable = complexKeysShardingValue.getLogicTableName()
                            + Separator.UNDERLINE.getSymbol() + item;
                    list.add(actualTable);
                });
            }
        }

        //插入时唯一分片
        if (CollectionUtils.isNotEmpty(expireTimes)) {

            Date expireDate = new Date(expireTimes.get(0));

            String tableSuffix = complexKeysShardingValue.getLogicTableName() + Separator.UNDERLINE.getSymbol()
                    + DateUtils.formatDate(expireDate, DateUtils.yyyyMMdd);
            list.add(tableSuffix);
        }

        return list;
    }
}