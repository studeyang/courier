package com.github.open.courier.core.transport;

/**
 * 消费模式
 */
public enum Mode {

    /**
     * push模式, 由courier-consumer拉取消息并推送到client消费, pause机制待开发
     */
    PUSH,

    /**
     * pull模式, 由courier-client拉取消息并直接消费, 有pause机制
     */
    PULL
}
