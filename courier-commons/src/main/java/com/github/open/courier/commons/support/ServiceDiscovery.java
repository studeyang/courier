package com.github.open.courier.commons.support;

import java.util.List;

/**
 * 服务发现
 *
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/5/27
 */
public interface ServiceDiscovery {

    /**
     * 获取服务host+port集合
     *
     * @param service 服务名
     * @return host+port集合
     */
    List<String> getServiceHostAndPort(String service);

}
