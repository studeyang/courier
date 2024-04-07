package com.github.open.courier.core.exception;

/**
 * 订阅关系异常
 */
public class SubscribeException extends BaseMessageException {

    private static final long serialVersionUID = 6644428362487175046L;

    public SubscribeException(String message) {
        super(message);
    }

    public SubscribeException(String message, Throwable e) {
        super(message, e);
    }
}
