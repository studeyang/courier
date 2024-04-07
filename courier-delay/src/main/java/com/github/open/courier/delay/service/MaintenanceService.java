package com.github.open.courier.delay.service;

import com.github.open.courier.commons.support.CourierServerProperties;
import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.delay.core.ScheduleManager;
import com.github.open.courier.delay.core.support.metrics.TimeWheelMetrics;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "延迟消息运维服务")
@Slf4j
@RestController
public class MaintenanceService {

    @Autowired
    private ScheduleManager scheduleManager;

    @ApiOperation("清除Master节点竞争信号量")
    @DeleteMapping(URLConstant.DELAY_RACESIGN)
    public void clearRaceSign() {
        scheduleManager.clearRaceSign();
    }

    @ApiOperation("查询延迟消息配置信息")
    @GetMapping(URLConstant.DELAY_PROPERTIES)
    public CourierServerProperties.DelayProperties getDelayProperties() {
        return scheduleManager.getCourierDelayProperties();
    }

    @ApiOperation("查询时间轮监控指标信息")
    @GetMapping(URLConstant.DELAY_TIMEWHEEL_METRICS)
    public TimeWheelMetrics getTimeWheelMetrics() {
        return scheduleManager.getTimeWheelMetrics();
    }

}
