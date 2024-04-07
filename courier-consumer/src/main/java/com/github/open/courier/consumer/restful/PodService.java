package com.github.open.courier.consumer.restful;

import com.github.open.courier.core.constant.URLConstant;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author LIJIAHAO
 */
@Api(tags = "消费节点服务")
@Slf4j
@RestController
public class PodService {

    /**
     * 这个接口不需要了
     */
    @Deprecated
    @ApiOperation("获取多个服务的节点")
    @PostMapping(URLConstant.CONSUMER_PODS)
    public Map<String, List<String>> getPodsByServices(@RequestBody Set<String> services) {
        Map<String, List<String>> map = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(services)) {
            services.forEach(e -> {
                map.put(e, new ArrayList<>());
//                map.put(e, courierDiscoveryClient.getServiceHostAndPort(e));
            });
        }
        return map;
    }

}
