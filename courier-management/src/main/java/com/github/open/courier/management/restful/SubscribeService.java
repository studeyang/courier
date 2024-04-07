package com.github.open.courier.management.restful;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.github.open.courier.commons.support.ServiceRegistration;
import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.transport.*;
import com.github.open.courier.management.application.service.SubscribeAppService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "订阅关系管理服务")
@Slf4j
@RestController
public class SubscribeService {

    @Autowired
    private SubscribeAppService subscribeAppService;
    @NacosInjected
    private NamingService namingService;

    @PostMapping(URLConstant.MANAGEMENT_SUBSCRIBE_METADATAS_PAGE)
    public PageModel<SubscribeMetadataDTO> findMetaDatasByPage(@RequestBody QuerySubscribeMetaDataRequest request) {
        return subscribeAppService.findMetaDatasByPage(request);
    }

    @PostMapping(URLConstant.MANAGEMENT_SUBSCRIBE_ENVTAG_ASSIGN)
    public void assignMessagePushEnv(@RequestBody AssignMessagePushEnvRequest request) {
        subscribeAppService.assignMessagePushEnv(request);
    }

    @PostMapping(URLConstant.MANAGEMENT_SUBSCRIBE_ENVTAG_CLEAR)
    public void clearMessagePushEnv(@RequestBody ClearMessagePushEnvRequest request) {
        subscribeAppService.clearMessagePushEnv(request);
    }

    @GetMapping(URLConstant.MANAGEMENT_SUBSCRIBECLUSTERS_METADATA_CLUSTERS)
    public List<String> listAllClusterMetaDatas() throws NacosException {
        return namingService.getAllInstances("courier-agent")
                .stream()
                .map(Instance::getClusterName)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());
    }

    @GetMapping(URLConstant.MANAGEMENT_SUBSCRIBECLUSTERS_METADATA_ENVS)
    public List<SubscribeClusterDTO> listEnvMetaDatasByCluster(@RequestParam String cluster) throws NacosException {
        List<SubscribeClusterDTO> subscribeClusterDTOS = new ArrayList<>();
        namingService.getAllInstances("courier-agent", Collections.singletonList(cluster))
                .stream()
                .filter(instance -> instance.getMetadata().get(ServiceRegistration.AGENT_ENV) != null)
                .collect(Collectors.toList())
                .forEach(instance -> {
                    SubscribeClusterDTO subscribeClusterDTO = new SubscribeClusterDTO()
                            .setCluster(cluster)
                            .setEnv(instance.getMetadata().get(ServiceRegistration.AGENT_ENV))
                            .setUrl("http://" + instance.getIp() + ":" + instance.getPort() + URLConstant.CLIENT_RECEIVES);
                    subscribeClusterDTOS.add(subscribeClusterDTO);
                });
        return subscribeClusterDTOS;
    }
}
