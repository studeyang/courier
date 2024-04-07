package com.github.open.courier.core.exception;

/**
 * Debug模式未开启异常
 */
public class DebugNotOpenException extends BaseMessageException {

    private static final long serialVersionUID = 5671931984916851263L;

    public DebugNotOpenException() {
        super("Debug模式未开启，此操纵直在Debug模式下生效，请注意使用");
    }

}
