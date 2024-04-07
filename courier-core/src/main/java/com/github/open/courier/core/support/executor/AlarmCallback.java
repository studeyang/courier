package com.github.open.courier.core.support.executor;

import com.github.open.courier.core.transport.ThreadPoolAlarmMetadata;
import lombok.extern.slf4j.Slf4j;

public interface AlarmCallback {

    AlarmCallback DEFAULT = new DeaultAlarmCallback();

    /**
     * 执行告警通知
     */
    void alarm(ThreadPoolAlarmMetadata metadata);

    /**
     * 执行恢复通知
     */
    void recovery(ThreadPoolAlarmMetadata metadata);


    @Slf4j
    class DeaultAlarmCallback implements AlarmCallback {

        @Override
        public void alarm(ThreadPoolAlarmMetadata metadata) {
            log.info("触发告警, 此告警处理器是默认处理器，什么也不做");
        }

        @Override
        public void recovery(ThreadPoolAlarmMetadata metadata) {
            log.info("告警恢复, 此告警处理器是默认处理器，什么也不做");
        }
    }

}
