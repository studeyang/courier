package com.github.open.courier.core.exception;

/**
 * 无可用的事务异常
 */
public class NotActiveTransactionException extends BaseMessageException {

    private static final long serialVersionUID = 4089132088644202008L;

    public NotActiveTransactionException() {
        super("当前线程无可用的事务, 请在方法上加上@Transactional确保有可用事务, 或使用publish()方法发送非事务消息");
    }
}
