package com.github.open.courier.core.support;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import lombok.Getter;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.util.Assert;

import com.github.open.courier.core.support.TimedBuffer.RejectedStrategy.RejectedStrategies;
import com.google.common.collect.Queues;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * 定时缓冲, 每bufferSize个 或 每timeout(unit) 消费一次
 */
@Slf4j
public class TimedBuffer<E> implements Closeable {

    final int bufferSize;
    final long timeout;
    final TimeUnit unit;
    final Consumer<List<E>> bufferConsumer;
    @Getter
    final BlockingQueue<E> queue;
    final RejectedStrategy rejectedStrategy;
    final int coreConsumerSize;
    final ThreadFactory threadFactory;
    final AtomicBoolean closed;
    @Getter
    volatile ExecutorService consumers; // NOSONAR 懒加载, sonar误报

    /**
     * 每bufferSize个, 消费一次
     *
     * @param bufferSize     每bufferSize个元素
     * @param bufferConsumer 消费这批缓冲的消费者
     */
    public TimedBuffer(int bufferSize,
                       Consumer<List<E>> bufferConsumer) {
        this(bufferSize, Long.MAX_VALUE, TimeUnit.MILLISECONDS, bufferConsumer);
    }

    /**
     * 每timeout(unit), 消费一次
     *
     * @param timeout        每timeout(unit)
     * @param unit           单位
     * @param bufferConsumer 消费这批缓冲的消费者
     */
    public TimedBuffer(long timeout,
                       TimeUnit unit,
                       Consumer<List<E>> bufferConsumer) {
        this(Integer.MAX_VALUE, timeout, unit, bufferConsumer);
    }

    /**
     * 每bufferSize个 或 每timeout(unit) 消费一次
     *
     * @param bufferSize     每bufferSize个 或 每timeout(unit)
     * @param timeout        每bufferSize个 或 每timeout(unit)
     * @param unit           单位
     * @param bufferConsumer 消费这批缓冲的消费者
     */
    public TimedBuffer(int bufferSize,
                       long timeout,
                       TimeUnit unit,
                       Consumer<List<E>> bufferConsumer) {
        this(bufferSize, timeout, unit, bufferConsumer, 1);
    }

    public TimedBuffer(int bufferSize,
                       long timeout,
                       TimeUnit unit,
                       Consumer<List<E>> bufferConsumer,
                       int coreConsumerSize) {
        this(bufferSize, timeout, unit, bufferConsumer, coreConsumerSize, new LinkedBlockingQueue<>(defaultCapacity(bufferSize, coreConsumerSize)));
    }

    public TimedBuffer(int bufferSize,
                       long timeout,
                       TimeUnit unit,
                       Consumer<List<E>> bufferConsumer,
                       int coreConsumerSize,
                       BlockingQueue<E> queue) {
        this(bufferSize, timeout, unit, bufferConsumer, coreConsumerSize, queue, RejectedStrategies.RUN);
    }

    public TimedBuffer(int bufferSize,
                       long timeout,
                       TimeUnit unit,
                       Consumer<List<E>> bufferConsumer,
                       int coreConsumerSize,
                       BlockingQueue<E> queue,
                       RejectedStrategy rejectedStrategy) {
        this(bufferSize, timeout, unit, bufferConsumer, coreConsumerSize, queue, rejectedStrategy, new CustomizableThreadFactory("buffer-handler-"));
    }

    /**
     * 每bufferSize个 或 每timeout(unit) 消费一次
     *
     * @param bufferSize       每bufferSize个 或 每timeout(unit) 消费一次
     * @param timeout          每bufferSize个 或 每timeout(unit) 消费一次
     * @param unit             timeout的单位
     * @param bufferConsumer   消费这批缓冲的消费者
     * @param coreConsumerSize 消费者线程数, 默认为: 1
     * @param queue            缓冲队列, 默认长度为: bufferSize * (coreConsumerSize + 1)
     * @param rejectedStrategy 队列满了或close时的拒绝策略, 默认为: 在生产者线程阻塞消费
     * @param threadFactory    消费者线程工厂, 默认线程名称为: buffer-handler-[n]
     */
    public TimedBuffer(int bufferSize,
                       long timeout,
                       TimeUnit unit,
                       Consumer<List<E>> bufferConsumer,
                       int coreConsumerSize,
                       BlockingQueue<E> queue,
                       RejectedStrategy rejectedStrategy,
                       ThreadFactory threadFactory) {

        Assert.isTrue(bufferSize > 0, "bufferSize 必须大于0");
        Assert.isTrue(timeout > 0, "timeout 必须大于0");
        Assert.isTrue(coreConsumerSize > 0, "coreConsumerSize 必须大于0");

        this.bufferSize = bufferSize;
        this.timeout = timeout;
        this.unit = Objects.requireNonNull(unit);
        this.bufferConsumer = Objects.requireNonNull(bufferConsumer);
        this.queue = Objects.requireNonNull(queue);
        this.rejectedStrategy = Objects.requireNonNull(rejectedStrategy);
        this.coreConsumerSize = coreConsumerSize;
        this.threadFactory = Objects.requireNonNull(threadFactory);
        this.closed = new AtomicBoolean();
    }

    /**
     * 将元素放入buffer队列, 如果已满或已close则拒绝
     *
     * 1.check, 如果closed则拒绝
     * 2.offer, 如果false则拒绝, 如果true还需double-check
     * 3.double-check, 如果未closed则正常结束, 否则需要remove并拒绝
     * 4.remove, 如果true则拒绝, 如果false说明已经被消费
     *
     * <pre>
     *      if (closed) {
     *          reject(); // 1.如果closed, 则拒绝
     *      } else {
     *          if (!offer) {
     *              reject(); // 2.如果offer失败, 则拒绝
     *          } else {
     *              if (closed) { // 3.double-check
     *                  if (remove) { // 4.remove并拒绝
     *                      reject();
     *                  } else {
     *                      return; // 已被消费
     *                  }
     *              } else {
     *                  return; // 正常结束
     *              }
     *          }
     *      }
     * </pre>
     *
     * @see ThreadPoolExecutor#execute(Runnable) 的step-2
     */
    public void offer(E e) {
        init();
        if (closed.get() || !queue.offer(e) || closed.get() && queue.remove(e)) {
            rejectedStrategy.onReject(this, e);
        }
    }

    /**
     * 延迟初始化
     */
    void init() {
        if (consumers == null) {
            synchronized (this) {
                if (consumers == null) {
                    consumers = Executors.newFixedThreadPool(coreConsumerSize, threadFactory);
                    BufferHandler handler = new BufferHandler();
                    IntStream.range(0, coreConsumerSize).forEach(i -> consumers.execute(handler));
                }
            }
        }
    }

    @Override
    public void close() {
        closed.set(true);
        if (consumers != null) {
            consumers.shutdown();
        }
    }

    static int defaultCapacity(int bufferSize, int coreConsumerSize) {
        Assert.isTrue(bufferSize > 0, "bufferSize 必须大于0");
        Assert.isTrue(coreConsumerSize > 0, "coreConsumerSize 必须大于0");
        int size = bufferSize * (coreConsumerSize + 1);
        return size <= 0 /* overflow */ ? Integer.MAX_VALUE : size;
    }

    class BufferHandler implements Runnable {

        /**
         * 只要未close或queue不为空, 就每bufferSize个/每timeout(unit)秒, 获取一次buffer并消费
         */
        @SneakyThrows
        @Override
        public void run() {

            List<E> buffer = new ArrayList<>();

            while (!closed.get() || !queue.isEmpty()) {
                if (Queues.drain(queue, buffer, bufferSize, timeout, unit) > 0) {
                    try {
                        bufferConsumer.accept(buffer);
                    } catch (Exception e) {
                        log.error("consume buffer error", e);
                    } finally {
                        // 不能用clear(), 因为不确定bufferConsumer是否是异步消费
                        // 为了复用空的buffer, 只有在真正消费后, 才继续new一个
                        buffer = new ArrayList<>();
                    }
                }
            }
            log.info("BufferHandler finished");
        }
    }

    public interface RejectedStrategy {

        <E> void onReject(TimedBuffer<E> buffer, E e);

        enum RejectedStrategies implements RejectedStrategy {

            RUN {
                @Override
                public <E> void onReject(TimedBuffer<E> buffer, E e) {
                    buffer.bufferConsumer.accept(Collections.singletonList(e));
                }
            },

            ABORT {
                @Override
                public <E> void onReject(TimedBuffer<E> buffer, E e) {
                    throw new RejectedExecutionException("buffer已满或已关闭, size: " + buffer.queue.size() + ", closed: " + buffer.closed);
                }
            }
        }
    }
}
