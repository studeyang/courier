package com.github.open.courier.core.transport;

import java.util.Set;

import com.github.open.courier.core.listener.ListenerConfig;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 订阅的topic和group
 */
@ApiModel
@Data
@Accessors(chain = true)
/**
 * @EqualsAndHashCode(exclude = "types"), 是为了放在Set中, 只考虑topic + groupId的唯一性, types可以忽略
 * 是为了防止订阅接口 {@link com.xxx.courier.producer.service.SubscribeService#subscribe(SubscribeRequest)}
 * 参数中的Set<TopicGroup>, 出现错误格式:
 *   t1 - g1 - [e1, e2]
 *   t1 - g1 - [e2, e3]
 * java的client端的参数肯定不会是这样, 主要为了防止app、开思助手等别的客户端, 传这样乱七八糟的格式去订阅(谁知道呢)
 * todo 其实订阅接口可以传Map<TopiGroup, Set<String>> : topicGroup - types, 是不是就避免了这个问题? 以后再考虑这样优化
 */
@EqualsAndHashCode(exclude = "types")
public class TopicGroup implements ListenerConfig {

    @ApiModelProperty("订阅的Topic")
    private String topic;

    @ApiModelProperty("消费组ID")
    private String groupId;

    @ApiModelProperty("订阅的消息类型(全限定名)")
    private Set<String> types;
}
