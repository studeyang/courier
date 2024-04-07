package com.github.open.courier.delay.core.sentinel;


import com.github.open.courier.delay.core.ScheduleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Master节点续期哨兵
 *
 * @author wangyonglin
 */
@Slf4j
public class MasterNodeRenewSentinel {


    private final ScheduleManager scheduleManager;

    private ScheduledExecutorService executor;


    public MasterNodeRenewSentinel(ScheduleManager scheduleManager) {
        this.scheduleManager = scheduleManager;
    }


    public void start() {
        log.info("启动Master节点续期哨兵, 节点IP：{}", scheduleManager.getNodeIpAddr());
        executor = Executors.newSingleThreadScheduledExecutor(new CustomizableThreadFactory("master-node-renewer-"));
        executor.scheduleAtFixedRate(new MasterNodeRenewer(), 0, scheduleManager.getMasterNodeRenewTime(), TimeUnit.MILLISECONDS);
    }


    public void stop() {
        log.info("停止Master节点续期哨兵, 节点IP：{}", scheduleManager.getNodeIpAddr());
        executor.shutdown();
    }


    class MasterNodeRenewer implements Runnable {

        @Override
        public void run() {
            try {
                doRenewaCheck();
            } catch (Exception e) {
                log.error("续期检查异常", e);
            }
        }


        /**
         *    防止主节点在续期时网络阻塞，导致过期，其他节点晋升为Maste节点，
         * 出现多Master情况, 所以在续期成功后需要检查Redis里面的存的信号量
         * 是否和当前一致，不一致代表其他节点已经晋升为Master节点，此节点需要
         * 降级为Salve节点
         */
        public void doRenewaCheck() {

            if (scheduleManager.renewMasterNode()) {

                log.info("Master节点状态健康, 继续保持续期检查状态");

                return;
            }

            scheduleManager.degradeSlaveNode();
        }

    }


}
