package com.github.open.courier.commons.loadbalance;

/**
 * 负载均衡异常
 *
 * @author yanglulu
 */
public class LoadBalanceException extends RuntimeException {

    public LoadBalanceException(String msg) {
        super(msg);
    }

}
