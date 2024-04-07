package com.github.open.courier.client.consumer.internal;

import com.github.open.courier.client.feign.ProducerClient;
import com.github.open.courier.core.support.CourierContext;
import com.github.open.courier.core.constant.ClientState;
import com.github.open.courier.core.support.executor.PauseCallback;
import com.github.open.courier.core.transport.PausePushRequest;
import com.github.open.courier.core.transport.ResumePushRequest;
import com.github.open.courier.core.utils.IpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 消费暂停处理器
 */
@Slf4j
public class PauseCallbackFactory {


    public static PauseCallback newInstance(ProducerClient producerClient) {
        return new PushModeCallback(producerClient);
    }

    /**
     * 【PULL】 模式下的暂停处理器
     */
    static class PullModeCallback implements PauseCallback {

        @Override
        public void pause() {

            log.warn("【PULL】发送暂停消费请求，service: [{}]", CourierContext.getService());

            PullConsumerListener.PullSwitch.pause();
        }

        @Override
        public void resume() {

            log.warn("【PULL】发送恢复消费请求，service: [{}]", CourierContext.getService());

            PullConsumerListener.PullSwitch.resume();
        }
    }


    /**
     * 【PUSH】 模式下的暂停处理器
     */
    @RequiredArgsConstructor
    static class PushModeCallback implements PauseCallback {

        final ProducerClient producerClient;

        @Override
        public void pause() {

            log.warn("【PUSH】发送暂停消费请求，service: [{}]", CourierContext.getService());

            PausePushRequest request = new PausePushRequest()
                    .setIp(IpUtils.getHostAddress())
                    .setPort(CourierContext.getServerPort())
                    .setService(CourierContext.getService())
                    .setClientState(ClientState.TRY_PAUSE);

            producerClient.pausePush(request);
        }

        @Override
        public void resume() {

            log.warn("【PUSH】发送恢复消费请求，service: [{}]", CourierContext.getService());

            ResumePushRequest request = new ResumePushRequest()
                    .setIp(IpUtils.getHostAddress())
                    .setPort(CourierContext.getServerPort())
                    .setService(CourierContext.getService())
                    .setClientState(ClientState.TRY_RESUME);

            producerClient.resumePush(request);
        }
    }


}
