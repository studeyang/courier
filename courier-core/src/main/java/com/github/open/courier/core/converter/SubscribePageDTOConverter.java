package com.github.open.courier.core.converter;

import com.github.open.courier.core.transport.SubscribeManageDTO;
import com.github.open.courier.core.transport.SubscribeMetadata;
import com.github.open.courier.core.transport.SubscribePageDTO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 订阅服务页面数据转换器
 *
 * @author LIJIAHAO
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SubscribePageDTOConverter {


    /**
     * 前端页面要以服务名进行分组，所以将服务名相同的SubscribeMetadata转换成一个SubscribePageDTO
     *
     * @param subscribeMetadataList 数据库查询的订阅服务
     * @param manageDTOList         订阅管理数据
     * @param ipAndPortMap          服务的ip和端口map集合
     * @return 以服务名进行分组的订阅服务列表
     */
    public static List<SubscribePageDTO> subscribePageDTOList(List<SubscribeMetadata> subscribeMetadataList,
                                                              List<SubscribeManageDTO> manageDTOList, Map<String, List<String>> ipAndPortMap) {
        HashMap<String, SubscribePageDTO> map = Maps.newHashMap();
        List<SubscribePageDTO> list = Lists.newArrayList();

        if (CollectionUtils.isEmpty(subscribeMetadataList)) {
            return list;
        }

        subscribeMetadataList.forEach(e -> {
            String service = e.getService();
            SubscribePageDTO spDTO;
            if ((spDTO = map.get(service)) != null) {
                spDTO.getGroupIds().add(e.getGroupId());
                spDTO.getTopics().add(e.getTopic());
                spDTO.getTypes().add(e.getType());
            } else {
                spDTO = new SubscribePageDTO();
                spDTO.setService(service)
                        .setTopics(Sets.newHashSet(e.getTopic()))
                        .setGroupIds(Sets.newHashSet(e.getGroupId()))
                        .setTypes(Sets.newHashSet(e.getType()))
                        .setIpAndPort(Lists.newArrayList());
                map.put(service, spDTO);
                list.add(spDTO);
            }
        });
        Map<String, SubscribeManageDTO> manageDTOMap = CollectionUtils.isEmpty(manageDTOList) ?
                Maps.newHashMap() : manageDTOList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(SubscribeManageDTO::getService, Function.identity()));
        list.forEach(e -> {
            e.setIpAndPort(ipAndPortMap.get(e.getService()));
            SubscribeManageDTO m = manageDTOMap.get(e.getService());
            if (m != null) {
                e.setConsumerNode(m.getConsumerNode());
                e.setEnable(m.isEnable());
            }
        });
        return list;
    }

}
