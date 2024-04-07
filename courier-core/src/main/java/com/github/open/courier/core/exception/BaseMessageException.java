package com.github.open.courier.core.exception;

/**
 * 消息异常的基类
 */
public abstract class BaseMessageException extends RuntimeException {

    private static final long serialVersionUID = -1360523446232641163L;

    public BaseMessageException() {
        super();
    }

    public BaseMessageException(String message) {
        super(message);
    }

    public BaseMessageException(Throwable throwable) {
        super(throwable);
    }

    public BaseMessageException(String message, Throwable e) {
        super(message, e);
    }
}
