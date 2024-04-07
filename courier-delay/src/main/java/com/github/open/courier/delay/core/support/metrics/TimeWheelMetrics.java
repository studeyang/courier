package com.github.open.courier.delay.core.support.metrics;

import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class TimeWheelMetrics {

    /**
     * 是否主节点
     */
    private boolean isMasterNode;

    /**
     * 时间轮开始时间
     */
    private long startTime;

    /**
     * 时间轮结束时间
     */
    private long endTime;

    /**
     * 时间轮上次跳动的位置
     */
    private int lastSkipPoint;

    /**
     * 查询监控信息耗时
     */
    private long spendTime;

    /**
     * 时间轮存储的消息映射
     */
    private Map<String, Set<String>> messageMap;

}
