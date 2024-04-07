package com.github.open.courier.consumer.restful;

import com.github.open.courier.consumer.service.listener.ConsumerListenerContainer;
import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.listener.AbstractConsumerListener;
import com.github.open.courier.core.listener.AbstractListenerContainer;
import com.github.open.courier.core.listener.ListenerConfig;
import com.github.open.courier.repository.biz.SubscribeBizService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Api(tags = "指标服务")
@RestController
@RequiredArgsConstructor
public class ConsumerMetricsService {

    final SubscribeBizService subscribeBizService;
    final ConsumerListenerContainer listenerContainer;

    @ApiOperation("获取Consumer的指标")
    @GetMapping(URLConstant.CONSUMER_METRICS)
    public ConsumerMetrics metrics() {

        // 统计listener数量, topic + groupId, 然后去重
        // 只查可用的订阅服务，排除已经下线的服务 ljh 2021.6.22
        long listenerCount = subscribeBizService.listEnableService()
                .stream()
                .map(ListenerConfig::getName)
                .distinct()
                .count() + 1;

        return new ConsumerMetrics(listenerContainer, listenerCount);
    }

    @Getter
    static class ConsumerMetrics {

        final int maximumPoolSize;
        final int poolSize;
        final int activeCount;
        final long keepAliveSeconds;
        final boolean consistency;
        final Collection<AbstractConsumerListener> listeners;

        public ConsumerMetrics(AbstractListenerContainer listenerContainer, long listenerCount) {
            ThreadPoolExecutor pool = (ThreadPoolExecutor) listenerContainer.getListenerPool();
            listeners = listenerContainer.getListenerMap().values();
            this.maximumPoolSize = pool.getMaximumPoolSize();
            this.poolSize = pool.getPoolSize();
            this.activeCount = pool.getActiveCount();
            this.keepAliveSeconds = pool.getKeepAliveTime(TimeUnit.SECONDS);
            this.consistency = pool.getPoolSize() == listenerCount;
        }
    }
}
