package com.github.open.courier.delay.core.schedule;

import com.github.open.courier.delay.core.ScheduleManager;
import com.github.open.courier.repository.mapper.DelayMessageMapper;
import com.github.open.courier.core.transport.DelayMessage;
import com.github.open.courier.delay.core.support.preread.PreReadSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * 延迟消息预读取器
 * <p>
 * 问题1：为什么预读下线时间不取当前时间，而是要获取上一轮时间轮的结束时间？
 * <p>
 * 问题2：为什么预读线程要由拨动线程执行到最后一格（59索引）唤醒, 预读完成后
 * 再通知拨动线程执行，而不是预读线程和拨动线程分开进行，互不相干？
 * <p>
 * 问题3：让预读线程和拨动线程交替执行，也就是说等预读完成后，可能时间轮的刻度
 * 跳过了，中间会有消息未执行到的情况？
 *
 * @author wangyonglin
 */
@Slf4j
public class MessagePreReader {

    /**
     * 延迟消息预读器和时间轮刻度拨动器的通信协调者
     */
    private final PreReadAndSkipCoordinator coordinator;
    private volatile boolean readerStop;
    private Thread reader;

    private final RedisTimeWheel redisTimeWheel;
    private final DelayMessageMapper delayMessageMapper;
    private final PreReadSupport preReadSupport;

    public MessagePreReader(PreReadAndSkipCoordinator coordinator,
                            ScheduleManager scheduleManager,
                            PreReadSupport preReadSupport) {

        this.coordinator = coordinator;
        this.redisTimeWheel = scheduleManager.getRedisTimeWheel();
        this.delayMessageMapper = scheduleManager.getDelayMessageMapper();

        this.preReadSupport = preReadSupport;
    }


    public void start() {
        log.info("启动延迟消息预读取器，启动时间：{}", new Date());
        reader = new Thread(new PreReader(), "message_reader");
        reader.setDaemon(true);
        reader.start();
    }


    public void stop() {
        log.info("停止延迟消息预读取器，停止时间：{}", new Date());
        readerStop = true;
        reader.interrupt();
    }


    class PreReader implements Runnable {

        @Override
        public void run() {

            while (!readerStop) {

                if (coordinator.isOffline()) {
                    log.info("当前节点处于已下线状态，不进行预读操作");
                    return;
                }

                try {
                    this.doMessagePreRead();
                } catch (Exception e) {
                    log.error("预读数据失败", e);
                }

                coordinator.notifySkip();

                coordinator.waitRead();
            }
        }


        public void doMessagePreRead() {

            long monitorStartTime = System.currentTimeMillis();

            long preReadDownLineTime = redisTimeWheel.calculateForwardSearchTime(monitorStartTime);

            long wheelEndTime = redisTimeWheel.getEndTime();

            long preReadUpLineTime = wheelEndTime + redisTimeWheel.getPerCycleTime();

            List<DelayMessage> delayMessages = preReadMessageFromDB(preReadDownLineTime, preReadUpLineTime);

            if (CollectionUtils.isNotEmpty(delayMessages)) {

                redisTimeWheel.push(delayMessages);

                preReadSupport.submitMarkReadedTask(delayMessages, preReadDownLineTime, preReadUpLineTime);
            }

            redisTimeWheel.setStartTimeAndEndTime(wheelEndTime, preReadUpLineTime);

            long monitorEndTime = System.currentTimeMillis();

            log.info("延迟消息预读操作执行完成，操作开始时间：{}，操作结束时间：{}, 预读耗时：{}",
                    monitorStartTime, monitorEndTime, monitorEndTime - monitorStartTime);
        }


        private List<DelayMessage> preReadMessageFromDB(long preReadDownLineTime, long preReadUpLineTime) {

            long monitorStartTime = System.currentTimeMillis();

            List<DelayMessage> delayMessages = delayMessageMapper
                    .listNeedPreReadByExpireTimeRange(preReadDownLineTime, preReadUpLineTime);

            long monitorEndTime = System.currentTimeMillis();

            log.info("读取在指定范围的延迟消息，预读下限时间：{}, 预读上限时间：{}, 消息size：{}，操作开始时间：{}，操作结束时间：{}, 读取耗时：{}",
                    preReadDownLineTime, preReadUpLineTime, delayMessages.size(), monitorStartTime, monitorEndTime, monitorEndTime - monitorStartTime);

            return delayMessages;
        }


    }


}
