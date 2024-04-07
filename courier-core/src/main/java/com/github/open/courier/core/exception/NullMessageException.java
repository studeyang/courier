package com.github.open.courier.core.exception;

/**
 * 消息为null异常
 */
public class NullMessageException extends BaseMessageException {

    private static final long serialVersionUID = 5671931984916851755L;

    public NullMessageException(String message) {
        super(message);
    }
}
