package com.github.open.courier.core.support.executor;

import lombok.extern.slf4j.Slf4j;

/**
 * 暂停处理器回调
 */
public interface PauseCallback {

    PauseCallback DEFAULT = new DeaultPauseCallback();

    /**
     * 执行暂停操作
     */
    void pause();

    /**
     * 执行恢复操作
     */
    void resume();

    @Slf4j
    class DeaultPauseCallback implements PauseCallback {

        @Override
        public void pause() {
            log.info("触发暂停, 此暂停处理器是默认处理器，什么也不做");
        }

        @Override
        public void resume() {
            log.info("触发恢复, 此暂停处理器是默认处理器，什么也不做");
        }
    }

}
