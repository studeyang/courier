package com.github.open.courier.management.restful;

import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.converter.SubscribePageDTOConverter;
import com.github.open.courier.core.transport.*;
import com.github.open.courier.management.infrastructure.feign.ConsumerClient;
import com.github.open.courier.repository.biz.SubscribeBizService;
import com.github.open.courier.repository.mapper.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AdminService {

    private final MessageMapper messageMapper;
    private final ConsumeRecordMapper recordMapper;
    private final ConsumeFailMessageMapper consumeFailMapper;
    private final SendFailMessageMapper sendFailMapper;
    private final SubscribeBizService subscribeBizService;
    private final SubscribeManageMapper subscribeManageMapper;
    @Autowired
    private ConsumerClient consumerClient;

    @PostMapping(URLConstant.MANAGEMENT_QUERY_SEND_SUCCESS)
    public CommonPageDTO querySendSuccess(@RequestBody QuerySendMessageRequest request) {

        List<DBMessage> data = messageMapper.query(request);

        // count + like %content% 会非常非常非常慢, 所以在查询content时, 不count
        int count = 1000;
        // 可以最多就展示1000条记录

        return new CommonPageDTO(data, count, count);
    }

    @PostMapping(URLConstant.MANAGEMENT_QUERY_SEND_FAIL)
    public CommonPageDTO querySendFail(@RequestBody QuerySendMessageRequest request) {

        List<ConsumeFailMessage> data = sendFailMapper.query(request);

        return new CommonPageDTO(data, 1000, 1000);
    }

    @PostMapping(URLConstant.MANAGEMENT_QUERY_CONSUME_SUCCESS)
    public CommonPageDTO queryConsumeSuccess(@RequestBody QueryConsumeMessageRequest request) {

        List<ConsumeRecord> data = recordMapper.query(request);

        return new CommonPageDTO(data, 1000, 1000);
    }

    @PostMapping(URLConstant.MANAGEMENT_QUERY_CONSUME_FAIL)
    public CommonPageDTO queryConsumeFail(@RequestBody QueryConsumeMessageRequest request) {

        List<ConsumeFailMessage> data = consumeFailMapper.query(request);

        return new CommonPageDTO(data, 1000, 1000);
    }

    @PostMapping(URLConstant.MANAGEMENT_QUERY_ALL_SUBSCRIBE_MANAGE)
    public List<SubscribePageDTO> queryAllSubscribePageDTO() {

        return creatSubscribePageDTO(null);

    }

    @PostMapping(URLConstant.MANAGEMENT_QUERY_SUBSCRIBE_MANAGE)
    public List<SubscribePageDTO> queryOneSubscribePageDTO(@RequestBody String serviceName) {

        return creatSubscribePageDTO(serviceName);

    }

    private List<SubscribePageDTO> creatSubscribePageDTO(String serviceName) {
        List<SubscribeMetadata> list;

        List<SubscribeManageDTO> manageDTOList;

        if (StringUtils.isEmpty(serviceName)) {

            //查询全部订阅关系
            list = subscribeBizService.listEnableService();
            manageDTOList = subscribeManageMapper.queryAll();
        } else {
            //查询某个服务订阅关系
            list = subscribeBizService.queryByService(serviceName);
            manageDTOList = Lists.newArrayList(subscribeManageMapper.queryByService(serviceName));
        }

        if (CollectionUtils.isNotEmpty(list)) {

            Set<String> serviceNameSet = Sets.newHashSet();

            list.forEach(e -> serviceNameSet.add(e.getService()));

            return SubscribePageDTOConverter.subscribePageDTOList(list, manageDTOList, consumerClient.getPodsByServices(serviceNameSet));

        } else {
            return Lists.newArrayList();
        }
    }

    @PostMapping(URLConstant.MANAGEMENT_SUBSCRIBE_BIND)
    public int subscribeBind(@RequestBody SubscribeManageRequest request) {

        SubscribeManageDTO subscribeManageDTO = subscribeManageMapper.queryByService(request.getService());

        if (subscribeManageDTO != null) {

            return subscribeManageMapper.update(subscribeManageDTO.setConsumerNode(request.getConsumerNode()));

        } else {
            return subscribeManageMapper.insert(new SubscribeManageDTO()
                    .setService(request.getService())
                    .setConsumerNode(request.getConsumerNode())
                    .setEnable(true));
        }

    }

}
