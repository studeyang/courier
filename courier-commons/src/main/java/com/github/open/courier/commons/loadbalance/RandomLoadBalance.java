package com.github.open.courier.commons.loadbalance;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author yangllu
 */
public class RandomLoadBalance implements ILoadBalance {

    @Override
    public String choose(List<String> hosts) {

        if (CollectionUtils.isEmpty(hosts)) {
            return StringUtils.EMPTY;
        }

        int random = RandomUtils.nextInt(0, hosts.size());

        return hosts.get(random);
    }

    @Override
    public LoadBalanceAlgorithm algorithm() {
        return LoadBalanceAlgorithm.RANDOM;
    }

}
