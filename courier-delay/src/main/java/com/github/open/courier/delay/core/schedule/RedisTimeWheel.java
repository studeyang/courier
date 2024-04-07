package com.github.open.courier.delay.core.schedule;

import com.github.open.courier.commons.redis.RedisHelper;
import com.github.open.courier.delay.core.ScheduleManager;
import com.github.open.courier.delay.core.support.metrics.TimeWheelMetrics;
import com.github.open.courier.commons.support.CourierServerProperties;
import com.github.open.courier.core.transport.DelayMessage;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.core.RedisCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * Reddis时间轮
 *
 * @author wangyonglin
 */
@Slf4j
public class RedisTimeWheel {

    /**
     * 时间轮开始时间对应的rediskey
     */
    private final String startTimeRedisKey;
    /**
     * 时间轮结束时间对应的rediskey
     */
    private final String endTimeRedisKey;
    /**
     * 刻度对应rediskey列表
     */
    private final String[] pointRedisKeys;


    /**
     * 时间轮开始时间
     */
    private long startTime;
    /**
     * 时间轮结束时间
     */
    private long endTime;
    /**
     * 时间轮每圈的时长
     */
    private final long perCycleTime;
    /**
     * 时间轮每刻度的时长
     */
    private final long perGridtime;
    /**
     * 时间轮总刻度数
     */
    private final int totalGrids;
    /**
     * 时间轮开始位置
     */
    private final int startPoint;
    /**
     * 时间轮上次跳动的位置
     */
    private int lastSkipPoint;


    /**
     * 以当前时间为准，未来可以推入时间轮的间隔时长
     */
    private final long canPushDiffTime;
    /**
     * 向前搜索补偿的时间间隔：主要用户防止临界点的情况
     */
    private final long forwardSearchTimeInterval;

    /**
     * 向后搜索补偿的时间间隔：主要用户防止临界点的情况
     */
    private final long backwardSearchTimeInterval;


    /**
     * 推入时间轮时管道里每个命令支持的消息量
     */
    private final int pushPartitionSize;
    /**
     * 从时间轮删除时管道里每个命令支持的消息量
     */
    private final int deletePartitionSize;


    private final RedisHelper redisHelper;
    private final ScheduleManager scheduleManager;


    public RedisTimeWheel(ScheduleManager scheduleManager) {

        CourierServerProperties.DelayProperties.TimeWheelProperties
                properties = scheduleManager.getCourierDelayProperties().getTimeWheel();

        this.pushPartitionSize = properties.getPushPartitionSize();
        this.deletePartitionSize = properties.getDeletePartitionSize();

        this.perCycleTime = properties.getPerCycleTime();
        this.perGridtime = properties.getPerGridTime();
        this.startPoint = properties.getStartPoint();

        String pointerSuffix = properties.getPointerSuffix();

        // 计算并初始化时间轮的总刻度数
        this.totalGrids = perCycleTime % perGridtime == 0
                ? (int) (perCycleTime / perGridtime) : (int) ((perCycleTime / perGridtime) + 1);

        // 计算并初始化未来可以推入时间轮的间隔时长, 最多可支持跨时间轮一圈的时长，跨2圈就出问题了，不好解释，我也想了好久。。
        this.canPushDiffTime = (totalGrids - 1) * perGridtime;
        this.forwardSearchTimeInterval = perCycleTime * properties.getForwardSearchThresholdRate();
        this.backwardSearchTimeInterval = perCycleTime * properties.getBackwardSearchThresholdRate();

        // 计算并初始化时间轮刻度对应的Rediskey列表, 为了方便计算，让时间轮的下标从1开始, 数组为0的位置为空
        this.pointRedisKeys = new String[totalGrids + 1];

        for (int i = 1; i <= totalGrids; i++) {
            pointRedisKeys[i] = String.format(pointerSuffix, i);
        }

        this.startTimeRedisKey = properties.getStartTimeRedisKey();
        this.endTimeRedisKey = properties.getEndTimeRedisKey();

        this.redisHelper = scheduleManager.getRedisHelper();
        this.scheduleManager = scheduleManager;
    }


    /**
     * 推入一个消息
     */
    public boolean push(DelayMessage delayMessage) {

        long monitorStartTime = System.currentTimeMillis();

        long startTime = getStartTime();

        int targePoint = calculatePushPoint(startTime, delayMessage.getExpireTime());

        String messageId = delayMessage.getMessageId();

        String pointRedisKey = pointRedisKeys[targePoint];

        Long size = redisHelper.add(pointRedisKey, messageId);

        long monitorEndTime = System.currentTimeMillis();

        log.info("延迟消息推入成功，消息ID：{}, 时间轮key：{}, 时间轮开始时间：{}, 结果：{}, 操作开始时间：{}, 操作结束时间：{}, 操作耗时：{}",
                messageId, pointRedisKey, startTime, size == 1, monitorStartTime, monitorEndTime, monitorEndTime - monitorStartTime);

        return true;
    }


    /**
     * 推入一批消息（到期时间不同）
     */
    public void push(List<DelayMessage> delayMessages) {

        long monitorStartTime = System.currentTimeMillis();

        long startTime = getStartTime();

        Map<String, List<DelayMessage>> groupByTargetPointMap = delayMessages.stream().collect(
                Collectors.groupingBy(
                        message -> pointRedisKeys[calculatePushPoint(startTime, message.getExpireTime())]
                )
        );

        pushByPipeline(groupByTargetPointMap);

        long monitorEndTime = System.currentTimeMillis();

        log.info("延迟消息批量推入成功(到期时间不同)，时间轮开始时间：{}, 操作开始时间：{}, 操作结束时间：{}, 操作耗时：{}",
                startTime, monitorStartTime, monitorEndTime, monitorEndTime - monitorStartTime);
    }


    /**
     * 推入一批消息（到期时间相同）
     */
    public boolean push(List<DelayMessage> delayMessages, long expireTime) {

        long monitorStartTime = System.currentTimeMillis();

        long startTime = getStartTime();

        int targePoint = calculatePushPoint(startTime, expireTime);

        String pointRedisKey = pointRedisKeys[targePoint];

        Map<String, List<DelayMessage>> groupByTargetPointMap = Maps.newHashMap();

        groupByTargetPointMap.put(pointRedisKey, delayMessages);

        pushByPipeline(groupByTargetPointMap);

        long monitorEndTime = System.currentTimeMillis();

        log.info("延迟消息批量推入成功(到期时间相同), 时间轮key：{}, 消息size：{}, 时间轮开始时间：{}, 操作开始时间：{}, 操作结束时间：{}, 操作耗时：{}",
                pointRedisKey, delayMessages.size(), startTime, monitorStartTime, monitorEndTime, monitorEndTime - monitorStartTime);

        return true;
    }


    /**
     * 通过管道分批推入延迟消息ID
     */
    private void pushByPipeline(Map<String, List<DelayMessage>> groupByTargetPointMap) {

        redisHelper.getRedisTemplate().executePipelined((RedisCallback<Object>) connection -> {

            groupByTargetPointMap.forEach((key, messages) -> {

                log.info("向时间轮推入消息, 时间轮key：{}, 消息size：{}", key, messages.size());

                Lists.partition(messages, pushPartitionSize).forEach(items -> {

                    List<byte[]> list = new ArrayList<>();

                    for (DelayMessage delayMessage : items) {
                        String messageIdStr = "\"" + delayMessage.getMessageId() + "\"";
                        list.add(messageIdStr.getBytes());
                    }

                    connection.sAdd(key.getBytes(), list.toArray(new byte[list.size()][]));
                });
            });

            return null;
        });
    }


    /**
     * 拉取消息
     */
    public List<String> poll(int targetPoint) {

        long monitorStartTime = System.currentTimeMillis();

        String targetPointRedisKey = pointRedisKeys[targetPoint];

        Set<Object> messageIdObjs = redisHelper.members(targetPointRedisKey);

        List<String> messageIds = messageIdObjs.stream().map(String::valueOf).collect(Collectors.toList());

        long monitorEndTime = System.currentTimeMillis();

        log.info("拉取redis时间轮延迟消息，目标索引：{}, 消息size：{}, 操作开始时间：{}, 操作结束时间：{}, 拉取耗时：{}",
                targetPoint, messageIds.size(), monitorStartTime, monitorEndTime, monitorEndTime - monitorStartTime);

        return messageIds;
    }


    /**
     * 删除消息
     */
    public void delete(int targetPoint, List<String> messageIds) {

        if (CollectionUtils.isEmpty(messageIds)) {
            log.info("消息ID集合为空，不进行删除逻辑");
            return;
        }

        long monitorStartTime = System.currentTimeMillis();

        String targetPointRedisKey = pointRedisKeys[targetPoint];

        List<Object> sizes = redisHelper.getRedisTemplate().executePipelined((RedisCallback<Object>) connection -> {

            Lists.partition(messageIds, deletePartitionSize).forEach(items -> {

                List<byte[]> list = new ArrayList<>();

                for (String messageId : items) {
                    String messageIdStr = "\"" + messageId + "\"";
                    list.add(messageIdStr.getBytes());
                }
                connection.sRem(targetPointRedisKey.getBytes(), list.toArray(new byte[list.size()][]));
            });

            return null;
        });

        Long deleteTotalSize = sizes.stream().map(s -> Long.valueOf(String.valueOf(s))).reduce(0L, Long::sum);

        if (messageIds.size() != deleteTotalSize) {
            log.warn("存在部分消息未删除成功, 预期删除size：{}, 实际删除size：{}", messageIds.size(), deleteTotalSize);
        }

        long monitorEndTime = System.currentTimeMillis();

        log.info("删除redis时间轮延迟消息，目标索引：{}, 预期删除size：{}, 实际删除size：{}, 操作开始时间：{}, 操作结束时间：{}, 删除耗时：{}",
                targetPoint, messageIds.size(), deleteTotalSize, monitorStartTime, monitorEndTime, monitorEndTime - monitorStartTime);
    }


    /**
     * 判断消息是否可以推入时间轮
     */
    public boolean canPush(long expireTime) {

        long currentTime = System.currentTimeMillis();

        boolean canPush = canPushDiffTime >= (expireTime - currentTime);

        log.info("检查是否可以推入时间轮，可推入的最大间隔时长：{}, 当前时间：{}, 消息到期时间：{}, 结果：{}",
                canPushDiffTime, currentTime, expireTime, canPush);

        return canPush;
    }


    /**
     * 计算推入时间轮的索引位置: 对应的数组下表（从1开始）
     */
    private int calculatePushPoint(long startTime, long expireTime) {

        int target;

        if (expireTime < startTime) {

            target = startPoint;

        } else {

            long diffTime = expireTime - startTime;

            target = diffTime % perGridtime == 0 ? (int) (diffTime / perGridtime) : (int) (diffTime / perGridtime) + 1;
        }

        return (target % totalGrids) == 0 ? totalGrids : (target % totalGrids);
    }


    /**
     * 计算拨动时间轮的索引位置: 要比推入的小一格位置
     */
    public int calculateSkipPoint() {

        long currentTime = System.currentTimeMillis();

        long startTime = getStartTime();

        int pushPoint = calculatePushPoint(startTime, currentTime);

        int skipPoint = pushPoint - 1;

        return skipPoint == 0 ? totalGrids : skipPoint;
    }


    /**
     * 设置时间轮的开始时间和结束时间
     */
    public void setStartTimeAndEndTime(long startTime, long endTime) {

        long monitorStartTime = System.currentTimeMillis();

        this.startTime = startTime;

        this.endTime = endTime;

        redisHelper.getRedisTemplate().executePipelined((RedisCallback<Object>) connection -> {

            connection.set(startTimeRedisKey.getBytes(), String.valueOf(startTime).getBytes());

            connection.set(endTimeRedisKey.getBytes(), String.valueOf(endTime).getBytes());

            return null;
        });

        long monitorEndTime = System.currentTimeMillis();

        log.info("设置时间轮的开始时间和结束时间，时间轮开始时间：{}, 时间轮结束时间：{}, 操作开始时间：{}, 操作结束时间：{}, 设置耗时：{}",
                startTime, endTime, monitorStartTime, monitorEndTime, monitorEndTime - monitorStartTime);
    }


    /**
     * 重置时间轮的状态信息
     */
    public void reset() {

        long monitorStartTime = System.currentTimeMillis();

        this.startTime = monitorStartTime;

        this.endTime = monitorStartTime;

        this.lastSkipPoint = 0;

        redisHelper.getRedisTemplate().executePipelined((RedisCallback<Object>) connection -> {

            connection.set(startTimeRedisKey.getBytes(), String.valueOf(monitorStartTime).getBytes());

            connection.set(endTimeRedisKey.getBytes(), String.valueOf(monitorStartTime).getBytes());

            return null;
        });

        long monitorEndTime = System.currentTimeMillis();

        log.info("重置时间轮的开始时间和结束时间，以及上次指针拨动的刻度，操作开始时间：{}, 操作结束时间：{}, 设置耗时：{}",
                monitorStartTime, monitorEndTime, monitorEndTime - monitorStartTime);
    }


    /**
     * 获取时间轮开始时间
     */
    public long getStartTime() {
        return scheduleManager.isMasterNode() ?
                this.startTime : Long.parseLong(String.valueOf(redisHelper.get(startTimeRedisKey)));
    }


    /**
     * 获取时间轮结束时间
     */
    public long getEndTime() {
        return scheduleManager.isMasterNode() ?
                this.endTime : Long.parseLong(String.valueOf(redisHelper.get(endTimeRedisKey)));
    }


    /**
     * 获取上一次时间轮拨动的刻度
     */
    public int getLastSkipPoint() {
        return this.lastSkipPoint;
    }


    /**
     * 设置上一次时间轮拨动的刻度
     */
    public void setLastSkipPoint(int lastSkipPoint) {
        this.lastSkipPoint = lastSkipPoint;
    }


    /**
     * 获取时间轮每圈的时长
     */
    public long getPerCycleTime() {
        return this.perCycleTime;
    }


    /**
     * 获取时间轮每刻度的时长
     */
    public long getPerGridtime() {
        return this.perGridtime;
    }


    /**
     * 获取时间轮总刻度
     */
    public int getTotalGrids() {
        return this.totalGrids;
    }


    /**
     * 获取时间轮开始点
     */
    public int getStartPoint() {
        return this.startPoint;
    }


    /**
     * 当前索引是否是时间轮开始索引
     */
    public boolean isCycleStartPoint(int currentSkipPoint) {
        return this.startPoint == currentSkipPoint;
    }


    /**
     * 当前索引是否是时间轮结束索引
     */
    public boolean isCycleEndPoint(int currentSkipPoint) {
        return this.totalGrids == currentSkipPoint;
    }


    /**
     * 计算向前搜索补偿时间
     */
    public long calculateForwardSearchTime(long calculateTime) {
        return calculateTime - this.forwardSearchTimeInterval;
    }


    /**
     * 计算向后搜索补偿时间
     */
    public long calculateBackwardSearchTime(long calculateTime) {
        return calculateTime + this.backwardSearchTimeInterval;
    }


    /**
     * 等待合适的执行点
     */
    public void waitExecuteTimePoint() {

        long currentTime = System.currentTimeMillis();

        long diffTime = currentTime - this.startTime;

        long waitTime = (this.perGridtime - diffTime % this.perGridtime) + 1;

        log.info("等待执行的时间点，时间轮开始时间：{}, 当前时间：{}, 差距时间：{}, 等待时间：{}", this.startTime, currentTime, diffTime, waitTime);

        try {
            TimeUnit.MILLISECONDS.sleep(waitTime);
        } catch (InterruptedException e) {
            log.warn("线程sleep被打断", e);
            Thread.currentThread().interrupt();
        }
    }


    /**
     * 获取时间轮监控指标信息
     */
    public TimeWheelMetrics metrics() {

        long monitorStartTime = System.currentTimeMillis();

        TimeWheelMetrics metrics = new TimeWheelMetrics();

        metrics.setMasterNode(scheduleManager.isMasterNode());
        metrics.setStartTime(getStartTime());
        metrics.setEndTime(getEndTime());
        metrics.setLastSkipPoint(getLastSkipPoint());

        Map<String, Set<String>> messageMap = Maps.newHashMap();

        List<Object> messageIds = redisHelper.getRedisTemplate().executePipelined((RedisCallback<Object>) connection -> {
            for (int i = 1; i <= totalGrids; i++) {
                connection.sMembers(pointRedisKeys[i].getBytes());
            }
            return null;
        });

        for (int i = 1; i <= totalGrids; i++) {
            messageMap.put(pointRedisKeys[i], (Set<String>) messageIds.get(i - 1));
        }

        metrics.setMessageMap(messageMap);

        long monitorEndTime = System.currentTimeMillis();

        metrics.setSpendTime(monitorEndTime - monitorStartTime);

        return metrics;
    }


}
