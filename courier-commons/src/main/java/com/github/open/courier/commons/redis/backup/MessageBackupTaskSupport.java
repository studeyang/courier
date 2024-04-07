package com.github.open.courier.commons.redis.backup;

import com.github.open.courier.commons.redis.RedisHelper;
import com.github.open.courier.repository.mapper.MessageMapper;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;

/**
 * 500 条消息提交 kafka 只需 7 毫秒，提交数据库平均 100 毫秒。异步入库
 */
public class MessageBackupTaskSupport {

    private final ExecutorService dbServiceExecutor;
    private final MessageMapper messageMapper;
    private final RedisHelper redisHelper;
    private final int backupTaskCount;
    private MessageBackupTask task;

    public MessageBackupTaskSupport(ExecutorService dbServiceExecutor,
                                    MessageMapper messageMapper,
                                    RedisHelper redisHelper,
                                    int backupTaskCount) {
        this.dbServiceExecutor = dbServiceExecutor;
        this.messageMapper = messageMapper;
        this.redisHelper = redisHelper;
        this.backupTaskCount = backupTaskCount;
        initExecutor();
    }

    private void initExecutor() {
        // execute 执行多少次，会启动多少个线程
        task = new MessageBackupTask(messageMapper, redisHelper);
        for (int i = 0; i < backupTaskCount; i++) {
            dbServiceExecutor.execute(task);
        }
    }

    @PreDestroy
    private void stop() {
        //停止任务
        if (task.isTaskSwitch()) {
            task.stop();
        }

        if (!dbServiceExecutor.isShutdown()) {
            dbServiceExecutor.shutdown();
        }
    }
}
