package com.github.open.courier.autoconfigure;

import com.github.open.courier.client.consumer.internal.CourierClientProperties;
import com.github.open.courier.client.feign.ManagementClient;
import com.github.open.courier.client.feign.ProducerClient;
import com.github.open.courier.client.producer.MessagePublisher;
import com.github.open.courier.client.producer.TransactionMessagePublisher;
import com.github.open.courier.client.producer.sender.ReportableSender;
import com.github.open.courier.client.producer.sender.RetryableSender;
import com.github.open.courier.client.producer.sender.Sender;
import com.github.open.courier.client.producer.sender.TransactionProducerSender;
import com.github.open.courier.client.producer.transaction.JdbcTransactionMessageMapper;
import com.github.open.courier.client.producer.transaction.TransactionMessageMapper;
import com.github.open.courier.core.support.AutoStartupLifecycle;
import com.github.open.courier.core.support.CourierContext;
import com.github.open.courier.eventing.BroadcastPublisher;
import com.github.open.courier.eventing.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static com.github.open.courier.core.constant.MessageConstant.TRANSACTION_MESSAGE_PHASE;

/**
 * @author Courier
 */
@Slf4j
public class TransactionMessageConfiguration extends AutoStartupLifecycle {

    @Override
    public void onStart() {

        DataSource dataSource;
        try {
            dataSource = CourierContext.getBean(DataSource.class);
        } catch (Exception e) {
            log.info("服务内没有使用到数据库, 不启动kafka事务消息");
            return;
        }

        TransactionMessageMapper messageMapper = getTransactionMessageMapper(dataSource);

        // 建表
        messageMapper.createTableIfNotExists();

        // 手动创建sender、publisher
        Sender sender = getTransactionSender(messageMapper);
        MessagePublisher publisher = getMessagePublisher(messageMapper, sender);

        // 注入publisher
        EventPublisher.setTransactionPublisher(publisher);
        BroadcastPublisher.setTransactionPublisher(publisher);

        log.info("kafka事务消息初始化完毕");
    }

    private TransactionMessageMapper getTransactionMessageMapper(DataSource dataSource) {
        JdbcTemplate jdbcTemplate;
        try {
            jdbcTemplate = CourierContext.getBean(JdbcTemplate.class);
        } catch (Exception e) {
            log.info("Spring容器内没有自动装配JdbcTemplate");
            jdbcTemplate = new JdbcTemplate(dataSource);
        }

        TransactionMessageMapper messageMapper = new JdbcTransactionMessageMapper(jdbcTemplate);
        return CourierContext.register(messageMapper);
    }

    private MessagePublisher getMessagePublisher(TransactionMessageMapper messageMapper, Sender sender) {
        MessagePublisher publisher = new TransactionMessagePublisher(sender, messageMapper,
                CourierContext.getBean(CourierClientProperties.class).getProducer().getPartitionSize());
        CourierContext.register(publisher);
        return publisher;
    }

    private Sender getTransactionSender(TransactionMessageMapper messageMapper) {
        int partition = CourierContext.getBean(CourierClientProperties.class).getProducer().getPartitionSize();
        TransactionProducerSender transactionProducerSender = new TransactionProducerSender(
                CourierContext.getBean(ProducerClient.class), messageMapper, partition);
        Sender retryableSender = new RetryableSender(transactionProducerSender);
        Sender sender = new ReportableSender(retryableSender,
                transactionProducerSender, CourierContext.getBean(ManagementClient.class), partition);
        return CourierContext.register("transactionProducerSender", sender);
    }

    @Override
    public int getPhase() {
        return TRANSACTION_MESSAGE_PHASE;
    }
}