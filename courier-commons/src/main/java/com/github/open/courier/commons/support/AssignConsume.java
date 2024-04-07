package com.github.open.courier.commons.support;

import com.github.open.courier.core.support.CourierContext;
import com.github.open.courier.repository.mapper.SubscribeManageMapper;
import com.github.open.courier.commons.loadbalance.ILoadBalance;
import com.github.open.courier.core.transport.SubscribeManageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author LIJIAHAO
 * 指定消费节点
 */
@Slf4j
@RequiredArgsConstructor
public class AssignConsume {

    private final SubscribeManageMapper subscribeManageMapper;
    private final ILoadBalance loadBalance;

    public String assignIfNecessary(List<String> hostList, String service) {

        String assign = queryAssignIpAndPort(service);

        if (StringUtils.isEmpty(assign)) {
            return loadBalance.choose(hostList);
        }

        return assign;
    }

    public String queryAssignIpAndPort(String service) {

        // 不是开发环境不能指定节点
        if (!CourierContext.isDevEnvironment()) {
            return null;
        }

        //测试环境如果有指定消费节点，则选择指定的消费节点消费
        SubscribeManageDTO subscribeManageDTO = subscribeManageMapper.queryByService(service);

        if (subscribeManageDTO == null) {
            return null;
        }

        String assign = subscribeManageDTO.getConsumerNode();

        if (StringUtils.isEmpty(assign) || StringUtils.equals("all", assign)) {
            return null;
        }

        return assign;
    }

}
