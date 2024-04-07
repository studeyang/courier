package com.github.open.courier.agent.service.biz;

import com.github.open.courier.agent.service.support.RestTemplatePusher;
import com.github.open.courier.core.message.Usage;
import com.github.open.courier.core.transport.ConsumeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消息接收逻辑
 *
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/5/27
 */
@Service
public class ReceiveBizService {

    @Autowired
    private RestTemplatePusher restTemplatePusher;
    @Autowired
    private BroadcastBizService broadcastBizService;

    public void receive(List<ConsumeMessage> consumeMessages) {

        Map<Boolean, List<ConsumeMessage>> map = consumeMessages
                .stream()
                .collect(Collectors.partitioningBy(m -> m.getUsage() == Usage.EVENT));

        // 事件消息
        String service = consumeMessages.stream()
                .map(ConsumeMessage::getToService)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("这批数据找不到服务名：" + consumeMessages));
        restTemplatePusher.pushByService(service, map.get(true));

        // 广播消息
        broadcastBizService.handle(map.get(false));
    }

}
