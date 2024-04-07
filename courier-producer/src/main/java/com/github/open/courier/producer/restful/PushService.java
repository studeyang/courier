package com.github.open.courier.producer.restful;

import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.converter.MessageJsonConverter;
import com.github.open.courier.commons.redis.RedisClient;
import com.github.open.courier.core.constant.ClientState;
import com.github.open.courier.core.constant.MessageConstant;
import com.github.open.courier.core.message.Subscribe;
import com.github.open.courier.core.message.subscribe.PausePushSubscribe;
import com.github.open.courier.core.message.subscribe.ResumePushSubscribe;
import com.github.open.courier.core.support.Wrapper;
import com.github.open.courier.core.transport.PausePushRequest;
import com.github.open.courier.core.transport.ResumePushRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.github.open.courier.core.constant.MessageConstant.SUBSCRIBE_TOPIC;

/**
 * 推送相关请求接口
 *
 * @author yanglulu
 */
@Api(tags = "推送相关请求接口")
@Slf4j
@RestController
@RequiredArgsConstructor
public class PushService {

    @Autowired
    private Producer<String, String> producer;

    @Autowired
    private RedisClient redisClient;

    @ApiOperation("发送暂停推送消息")
    @PostMapping(URLConstant.PRODUCER_PAUSE_PUSH)
    public void pause(@RequestBody PausePushRequest request) {

        log.info("{} request, service: {}, ip: {}, port: {}", request.getClientState(), request.getService(),
                request.getIp(), request.getPort());

        // 将暂停节点存入redis

        if (request.getClientState() == ClientState.TRY_PAUSE) {
            String redisKey = String.format(MessageConstant.PAUSE_LIST, request.getService());
            String redisValue = request.getIp() + ":" + request.getPort();
            redisClient.add(redisKey, redisValue);
        } else {
            // null 和 STOPPING 都走这里
            String redisKey = String.format(MessageConstant.STOPPED_LIST, request.getService());
            String redisValue = request.getIp() + ":" + request.getPort();
            redisClient.add(redisKey, redisValue, 5);
        }

        // 向 consumer 发送暂停请求

        if (ClientState.TRY_PAUSE == request.getClientState()) {
            Subscribe subscribe = new PausePushSubscribe()
                    .setIp(request.getIp())
                    .setPort(request.getPort())
                    .setService(request.getService());
            sendSubscribe(subscribe);
        }

    }

    @ApiOperation("发送恢复推送消息")
    @PostMapping(URLConstant.PRODUCER_RESUME_PUSH)
    public void resume(@RequestBody ResumePushRequest request) {

        log.info("{} request, service: {}, ip: {}, port: {}", request.getClientState(), request.getService(),
                request.getIp(), request.getPort());

        // 将暂停节点从redis移除

        if (request.getClientState() == ClientState.TRY_RESUME) {
            String redisKey = String.format(MessageConstant.PAUSE_LIST, request.getService());
            String redisValue = request.getIp() + ":" + request.getPort();
            redisClient.remove(redisKey, redisValue);
        } else {
            // null 和 STARTING 都走这里
            String redisKey = String.format(MessageConstant.STARTED_LIST, request.getService());
            String redisValue = request.getIp() + ":" + request.getPort();
            redisClient.add(redisKey, redisValue, 5);
            redisClient.remove(String.format(MessageConstant.STOPPED_LIST, request.getService()), redisValue);
        }

        // 向 consumer 发送恢复请求

        if (ClientState.TRY_RESUME == request.getClientState()) {
            Subscribe subscribe = new ResumePushSubscribe()
                    .setIp(request.getIp())
                    .setPort(request.getPort())
                    .setService(request.getService());

            sendSubscribe(subscribe);
        }
    }

    private void sendSubscribe(Subscribe subscribe) {

        String jsonSubscribe = MessageJsonConverter.toJson(subscribe);

        ProducerRecord<String, String> record = new ProducerRecord<>(Wrapper.wrapTopic(SUBSCRIBE_TOPIC), jsonSubscribe);

        log.info("发送 {} 消息...", subscribe.getClass().getSimpleName());
        try {
            producer.send(record).get();
        } catch (Exception e) {
            log.error("发送消息异常", e);
        }
    }

}
