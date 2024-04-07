package com.github.open.courier.core.transport;

public enum ConsumeState {

    /**
     * 消费中
     */
    COMMITED,

    /**
     * 推送失败
     */
    PUSH_FAIL,

    /**
     * 业务端执行 handle 方法失败
     */
    HANDLE_FAIL,

    /**
     * 已处理
     */
    HANDLED
}
