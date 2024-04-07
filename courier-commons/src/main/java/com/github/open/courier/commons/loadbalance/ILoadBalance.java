package com.github.open.courier.commons.loadbalance;

import java.util.List;

/**
 * @author yanglulu
 */
public interface ILoadBalance {

    /**
     * 获取节点
     *
     * @param hosts 节点集合
     * @return 单个节点
     */
    String choose(List<String> hosts);

    /**
     * 负载均衡算法配置
     *
     * @return 负载均衡算法
     */
    default LoadBalanceAlgorithm algorithm() {
        return LoadBalanceAlgorithm.ROLLING;
    }

}
