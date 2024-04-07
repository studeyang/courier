package com.github.open.courier.delay.core;

import com.github.open.courier.commons.redis.RedisHelper;
import com.github.open.courier.delay.biz.MessageBizService;
import com.github.open.courier.delay.core.support.metrics.TimeWheelMetrics;
import com.github.open.courier.repository.mapper.DelayMessageMapper;
import com.github.open.courier.commons.support.CourierServerProperties;
import com.github.open.courier.commons.support.CourierServerProperties.DelayProperties;
import com.github.open.courier.core.transport.DelayMessage;
import com.github.open.courier.delay.client.ManagementClient;
import com.github.open.courier.delay.core.schedule.DelayMessageScheduler;
import com.github.open.courier.delay.core.schedule.RedisTimeWheel;
import com.github.open.courier.delay.core.sentinel.MasterNodeRenewSentinel;
import com.github.open.courier.delay.core.sentinel.SlaveNodeHeartBeatSentinel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 延迟消息调度管理器
 *
 * @author wangyonglin
 */
@Slf4j
@Getter
@Component
public class ScheduleManager {

    /**
     * 当前节点IP
     */
    private String nodeIpAddr;
    /**
     * 是否是Master节点
     */
    private volatile boolean isMasterNode;
    /**
     * Master节点竞争信号量
     */
    private String raceSign;
    /**
     * Master节点竞争信号量过期时间
     */
    private long signExpireTime;
    /**
     * Master节点续期间隔时间
     */
    private long masterNodeRenewTime;
    /**
     * Slave节点心跳检查时间间隔
     */
    private long slaveNodeHeartBeatTime;

    /**
     * Redis时间轮模型
     */
    private RedisTimeWheel redisTimeWheel;
    /**
     * 延迟任务调度器
     */
    private DelayMessageScheduler delayMessageScheduler;
    /**
     * Master节点续期哨兵
     */
    private MasterNodeRenewSentinel masterNodeRenewSentinel;
    /**
     * Slave节点心跳哨兵
     */
    private SlaveNodeHeartBeatSentinel slaveNodeHeartBeatSentinel;


    private final DelayProperties courierDelayProperties;
    @Autowired
    private RedisHelper redisHelper;
    @Autowired
    private DelayMessageMapper delayMessageMapper;
    @Autowired
    private MessageBizService messageBizService;
    @Autowired
    private ManagementClient managementClient;

    public ScheduleManager(CourierServerProperties serverProperties) {
        this.courierDelayProperties = serverProperties.getDelay();
    }

    @PostConstruct
    public void start() {

        // 初始化延迟消息调度管理器
        initScheduleManager();

        // 竞争成为Mster节点
        if (raceMasterNode()) {

            // 标记当前节点为Master节点
            markCurrentNodeToMaster();

            // 启动延迟消息调度器
            startDelayMessageScheduler();

            // 启动主节点续约哨兵
            startMasterNodeRenewSentinel();

        } else {

            // 标记当前节点为Slave节点
            markCurrentNodeToSlave();

            // 启动Slave节点心跳哨兵
            startSlaveNodeBeatSentinel();
        }
    }


    @PreDestroy
    public void stop() {

        log.info("触发关机操作，对延迟调度模块进行优雅关闭");

        if (isMasterNode) {

            log.info("当前节点为Master节点，节点IP：{}", nodeIpAddr);

            // 停止主节点续约哨兵
            stopMasterNodeRenewSentinel();

            // 停止延迟消息调度器
            stopDelayMessageScheduler();

            // 让出主节点身份（这里只是单纯的标记自身，真正的让出还是通过从节点的心跳感知到的）
            markCurrentNodeToSlave();
        } else {

            log.info("当前节点为Slave节点，节点IP：{}", nodeIpAddr);

            // 停止Slave节点心跳哨兵
            stopSlaveNodeBeatSentinel();
        }
    }


    /**
     * 初始化延时消息调度上下文
     */
    @SneakyThrows
    public void initScheduleManager() {

        log.info("延迟消息服务启动，初始化延迟消息调度管理器");

        this.nodeIpAddr = InetAddress.getLocalHost().getHostAddress();

        this.raceSign = courierDelayProperties.getSentinel().getRaceSign();
        this.signExpireTime = courierDelayProperties.getSentinel().getSignExpireTime();
        this.masterNodeRenewTime = courierDelayProperties.getSentinel().getMasterNodeRenewTime();
        this.slaveNodeHeartBeatTime = courierDelayProperties.getSentinel().getSlaveNodeHeartBeatTime();

        this.redisTimeWheel = new RedisTimeWheel(this);
        this.delayMessageScheduler = new DelayMessageScheduler(this);
        this.masterNodeRenewSentinel = new MasterNodeRenewSentinel(this);
        this.slaveNodeHeartBeatSentinel = new SlaveNodeHeartBeatSentinel(this);
    }


    /**
     * 竞争主节点
     */
    public boolean raceMasterNode() {
        boolean raceMasterNodeResult = redisHelper.setIfAbsent(raceSign, nodeIpAddr);
        log.info("Master节点竞争，节点IP：{}, 竞争结果：{}", nodeIpAddr, raceMasterNodeResult);
        return raceMasterNodeResult;
    }


    /**
     * 续期主节点
     * 防止主节点在续期时网络阻塞，导致过期，其他节点晋升为Maste节点，出现多Master情况,
     * 所以在续期成功后需要检查Redis里面的存的信号量是否和当前一致，不一致代表其他节点已经晋
     * 升为Master节点，此节点需要降级为Salve节点
     */
    public boolean renewMasterNode() {

        String masterNodeIp = String.valueOf(redisHelper.get(raceSign));

        boolean renewMasterNodeResult;

        if (StringUtils.equals(nodeIpAddr, masterNodeIp)) {
            renewMasterNodeResult = redisHelper.expire(raceSign, signExpireTime, TimeUnit.MILLISECONDS);
        } else {
            log.info("当前节点IP和Master节点Ip不一致，无法续期，当前节点IP：{}, Master节点IP：{}", nodeIpAddr, masterNodeIp);
            renewMasterNodeResult = false;
        }

        log.info("Master节点续期，节点IP：{}, 续期结果：{}", nodeIpAddr, renewMasterNodeResult);
        return renewMasterNodeResult;
    }


    /**
     * 升级为Master节点
     */
    public void upgradeMasterNode() {

        log.info("执行升级操作, 当前节点升级为Mster节点, 节点IP：{}", nodeIpAddr);

        if (isMasterNode) {
            log.warn("当前节点处于Master节点状态，不支持升级操作");
            return;
        }

        markCurrentNodeToMaster();
        stopSlaveNodeBeatSentinel();

        startDelayMessageScheduler();
        startMasterNodeRenewSentinel();
    }


    /**
     * 降级为Slave节点
     */
    public void degradeSlaveNode() {

        log.info("执行降级操作, 当前节点降级为Slave节点, 节点IP：{}", nodeIpAddr);

        if (!isMasterNode) {
            log.warn("当前节点处于Slave节点状态，不支持降级级操作");
            return;
        }

        stopMasterNodeRenewSentinel();
        stopDelayMessageScheduler();

        markCurrentNodeToSlave();
        startSlaveNodeBeatSentinel();
    }


    /**
     * 标记当前节点为Master节点
     */
    public void markCurrentNodeToMaster() {
        log.info("标记当前节点为Master节点，节点Ip: {}", nodeIpAddr);
        this.isMasterNode = true;
    }


    /**
     * 标记当前节点为Slave节点
     */
    public void markCurrentNodeToSlave() {
        log.info("标记当前节点为Slave节点，节点Ip: {}", nodeIpAddr);
        this.isMasterNode = false;
    }


    /**
     * 清除竞争信号量
     */
    public void clearRaceSign() {
        log.info("清除Master节点竞争信号量");
        redisHelper.expire(raceSign, 1000, TimeUnit.MILLISECONDS);
    }

    /**
     * 启动延迟消息调度器
     */
    public void startDelayMessageScheduler() {
        delayMessageScheduler.start();
    }


    /**
     * 停止延迟消息调度器
     */
    public void stopDelayMessageScheduler() {
        delayMessageScheduler.stop();
    }


    /**
     * 启动主节点续约哨兵
     */
    public void startMasterNodeRenewSentinel() {
        masterNodeRenewSentinel.start();
    }


    /**
     * 停止主节点续约哨兵
     */
    public void stopMasterNodeRenewSentinel() {
        masterNodeRenewSentinel.stop();
    }


    /**
     * 启动从节点心跳哨兵
     */
    public void startSlaveNodeBeatSentinel() {
        slaveNodeHeartBeatSentinel.start();
    }


    /**
     * 停止从节点心跳哨兵
     */
    public void stopSlaveNodeBeatSentinel() {
        slaveNodeHeartBeatSentinel.stop();
    }


    /**
     * 判断消息是否可以推入时间轮: 在接受消息时使用
     */
    public boolean canPush(long expireTime) {
        return redisTimeWheel.canPush(expireTime);
    }


    /**
     * 推入一个消息到Redis时间轮
     */
    public boolean pushRedisTimeWheel(DelayMessage delayMessage) {
        return redisTimeWheel.push(delayMessage);
    }


    /**
     * 推入一批消息到Redis时间轮(到期时间相同)
     */
    public boolean pushRedisTimeWheel(List<DelayMessage> delayMessages, long expireTime) {
        return redisTimeWheel.push(delayMessages, expireTime);
    }


    /**
     * 推入一批消息到Redis时间轮(到期时间不同)
     */
    public void pushRedisTimeWheel(List<DelayMessage> delayMessages) {
        redisTimeWheel.push(delayMessages);
    }


    /**
     * 查询redis时间轮监控指标信息
     */
    public TimeWheelMetrics getTimeWheelMetrics() {
        return redisTimeWheel.metrics();
    }


}
