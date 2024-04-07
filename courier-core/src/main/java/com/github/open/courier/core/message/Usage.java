package com.github.open.courier.core.message;

import com.github.open.courier.messaging.Event;
import com.github.open.courier.messaging.Message;

/**
 * 用途
 */
public enum Usage {

    /**
     * 事件
     */
    EVENT,

    /**
     * 广播
     */
    BROADCAST;

    /**
     * 默认null为事件
     */
    public static Usage of(String name) {
        return name == null ? EVENT : Usage.valueOf(name);
    }

    /**
     * 根据类型判断是event还是broadcast
     */
    public static Usage of(Message message) {
        return message instanceof Event ? EVENT : BROADCAST;
    }
}
