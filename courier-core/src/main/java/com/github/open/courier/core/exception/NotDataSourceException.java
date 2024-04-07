package com.github.open.courier.core.exception;

/**
 * 无可用的数据源异常
 */
public class NotDataSourceException extends BaseMessageException {

    private static final long serialVersionUID = 2199188365843591154L;

    public NotDataSourceException(String message) {
        super(message);
    }
}
