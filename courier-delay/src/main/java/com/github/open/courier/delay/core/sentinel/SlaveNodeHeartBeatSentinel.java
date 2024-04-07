package com.github.open.courier.delay.core.sentinel;


import com.github.open.courier.delay.core.ScheduleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * slave节点心跳哨兵
 *
 * @author wangyonglin
 */
@Slf4j
public class SlaveNodeHeartBeatSentinel {


    private final ScheduleManager scheduleManager;


    private ScheduledExecutorService executor;


    public SlaveNodeHeartBeatSentinel(ScheduleManager scheduleManager) {
        this.scheduleManager = scheduleManager;
    }


    public void start() {
        log.info("启动Slave节点心跳哨兵, 节点IP：{}", scheduleManager.getNodeIpAddr());
        executor = Executors.newSingleThreadScheduledExecutor(new CustomizableThreadFactory("slave-node-beater-"));
        executor.scheduleAtFixedRate(new SlaveNodeHeartBeater(), 0, scheduleManager.getSlaveNodeHeartBeatTime(), TimeUnit.MILLISECONDS);
    }


    public void stop() {
        log.info("停止Slave节点心跳哨兵, 节点IP：{}", scheduleManager.getNodeIpAddr());
        executor.shutdown();
    }


    class SlaveNodeHeartBeater implements Runnable {

        @Override
        public void run() {
            try {
                doSlaveNodeHeartBeatCheck();
            } catch (Exception e) {
                log.error("心跳检查异常", e);
            }
        }

        public void doSlaveNodeHeartBeatCheck() {

            if (!scheduleManager.raceMasterNode()) {

                log.info("Master节点状态健康, 继续保持心跳检查状态");

                return;
            }

            scheduleManager.upgradeMasterNode();
        }
    }


}
