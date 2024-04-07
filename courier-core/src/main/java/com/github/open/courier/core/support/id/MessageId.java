package com.github.open.courier.core.support.id;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 消息ID生成器
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageId {

    /**
     * 获取id, 底层由ObjectId实现
     */
    public static String getId() {
        return ObjectId.getId();
    }
}
