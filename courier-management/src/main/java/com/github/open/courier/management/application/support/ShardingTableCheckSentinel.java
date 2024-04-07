package com.github.open.courier.management.application.support;

import com.github.open.courier.management.application.service.ShardingTableAppService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 创建分表巡检
 *
 * @author yanglulu
 */
@Slf4j
public class ShardingTableCheckSentinel {

    private final ShardingTableChecker shardingTableChecker;
    private final ScheduledExecutorService executorService;

    public ShardingTableCheckSentinel(ShardingTableAppService shardingTableAppService) {
        this.shardingTableChecker = new ShardingTableChecker(shardingTableAppService);
        this.executorService = new ScheduledThreadPoolExecutor(1, new CustomizableThreadFactory("TableShardingSentinel-"));
    }

    @PostConstruct
    public void start() {
       executorService.scheduleAtFixedRate(shardingTableChecker, 0, 1, TimeUnit.DAYS);
    }

    @AllArgsConstructor
    static class ShardingTableChecker implements Runnable {

        private final ShardingTableAppService shardingTableAppService;

        @Override
        public void run() {
            log.info("开始执行消息分表巡检，当前时间: {}", LocalDateTime.now());
            shardingTableAppService.createShardingTable();
        }
    }
}
