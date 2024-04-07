package com.github.open.courier.core.exception;

/**
 * 无可用的groupId异常
 */
public class NotActiveGroupIdException extends BaseMessageException {

    private static final long serialVersionUID = -4386520915946857670L;

    public NotActiveGroupIdException() {
        super("没有可用的GroupId, 尝试清除 courier_subscribe_group_id 相应的 GroupId");
    }
}
