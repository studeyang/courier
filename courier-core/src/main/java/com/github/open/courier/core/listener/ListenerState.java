package com.github.open.courier.core.listener;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息监听者的状态
 *
 * @author yangllu
 */
@Getter
@AllArgsConstructor
public enum ListenerState {

    /**
     * 运行
     */
    RUNING(1),

    /**
     * 暂停
     */
    PAUSE(2),

    /**
     * 恢复
     */
    RESUME(3),

    /**
     * 停止
     */
    STOP(4);

    /**
     * 状态码
     */
    private int state;

}
