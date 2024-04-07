package com.github.open.courier.agent.infrastructure.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/5/27
 */
@Data
@Slf4j
@ConfigurationProperties("courier.agent")
public class CourierAgentProperties implements InitializingBean {

    /**
     * 代理转发的目标 url
     */
    private String proxyUrl;

    private String proxyProducerPrefix = "/courier-producer";

    private String proxyDelayPrefix = "/courier-delay";

    private String proxyConsumerPrefix = "/courier-consumer";

    /**
     * courier agent 所在的集群
     */
    private String cluster;

    /**
     * courier agent 所在集群的环境
     */
    private String env = "default";

    private Map<String, String> discovery = new HashMap<>();

    /**
     * key: 别名，即在 courier 使用的名字
     * value: spring.application.name
     * <p>
     * 应用场景：
     * 1、agent.discover.scope 展示的是别名；<br/>
     * 2、服务发现获取 ip 会根据别名去映射真实名；
     */
    private Map<String, String> alias = new HashMap<>();

    /**
     * 不在 agent.discover.scope 展示的服务；
     */
    private Set<String> excludes = new HashSet<>();

    private boolean registerServiceScope = false;

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(proxyUrl, "proxyUrl 不可为空");
        log.info("Courier agent properties: {}", this);
    }

}
