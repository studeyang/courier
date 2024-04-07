package com.github.open.courier.management.application.service;

import com.github.open.courier.core.transport.*;
import com.github.open.courier.management.infrastructure.converter.SubscribeConverter;
import com.github.open.courier.management.infrastructure.feign.ProducerClient;
import com.github.open.courier.management.infrastructure.validator.SubscribeValidator;
import com.github.open.courier.repository.entity.SubscribeEntity;
import com.github.open.courier.repository.mapper.SubscribeMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class SubscribeAppService {

    @Autowired
    private ProducerClient producerClient;

    @Autowired
    private SubscribeMapper subscribeMapper;

    public PageModel<SubscribeMetadataDTO> findMetaDatasByPage(QuerySubscribeMetaDataRequest request) {

        PageHelper.startPage(request.getPage(), request.getSize());

        List<SubscribeEntity> subscribeEntities = subscribeMapper.findMetaDatasByPage(request);

        PageInfo<?> pageInfo = new PageInfo<>(subscribeEntities);

        List<SubscribeMetadataDTO> subscribeMetadatas = SubscribeConverter.toSubscribeMetadatas(subscribeEntities);

        return new PageModel<>(pageInfo.getTotal(), pageInfo.getPages(), pageInfo.getPageNum(), pageInfo.getPageSize(), subscribeMetadatas);
    }

    @Transactional(rollbackFor = Exception.class)
    public void assignMessagePushEnv(AssignMessagePushEnvRequest request) {

        log.info("【ASSIGN】指定消息推送的环境标记, request：{}", request);

        SubscribeValidator.validate(request);

        subscribeMapper.assignMessagePushEnv(request);

        producerClient.refreshAllListener();
    }

    @Transactional(rollbackFor = Exception.class)
    public void clearMessagePushEnv(ClearMessagePushEnvRequest request) {

        log.info("【CLEAR】清除消息推送的环境标记, request：{}", request);

        SubscribeValidator.validate(request);

        subscribeMapper.clearMessagePushEnv(request);

        producerClient.refreshAllListener();
    }

}
