package com.github.open.courier.core.exception;

/**
 * 找不到消息的topic异常
 */
public class NotTopicException extends BaseMessageException {

    private static final long serialVersionUID = -157576237833760898L;

    public NotTopicException(String message) {
        super(message);
    }
}
