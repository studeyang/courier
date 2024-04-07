package com.github.open.courier.core.constant;

/**
 * 客户端状态
 *
 * @author yanglulu
 * @date 2021/8/5
 */
public enum ClientState {

    /**
     * 请求暂停
     */
    TRY_PAUSE,

    /**
     * 请求恢复
     */
    TRY_RESUME,

    /**
     * 正在停止
     */
    STOPPING,

    /**
     * 正在启动
     */
    STARTING

}
