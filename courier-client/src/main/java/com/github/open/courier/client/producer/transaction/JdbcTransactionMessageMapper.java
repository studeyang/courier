package com.github.open.courier.client.producer.transaction;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import com.github.open.courier.core.transport.SendMessage;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JdbcTransactionMessageMapper implements TransactionMessageMapper {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void createTableIfNotExists() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `transaction_message`\n" +
                                     "        (\n" +
                                     "            `id`          varchar(32)  NOT NULL COMMENT '消息ID',\n" +
                                     "            `topic`       varchar(64)  NOT NULL COMMENT 'Topic',\n" +
                                     "            `type`        varchar(128) NOT NULL COMMENT '消息类型',\n" +
                                     "            `service`     varchar(64)  NOT NULL COMMENT '服务名',\n" +
                                     "            `content`     longtext     NOT NULL COMMENT '消息内容',\n" +
                                     "            `created_at`  datetime     NOT NULL COMMENT '创建时间',\n" +
                                     "            `primary_key` varchar(256) DEFAULT NULL COMMENT '消息主键',\n" +
                                     "            `usage`       varchar(16)  DEFAULT NULL COMMENT '用途',\n" +
                                     "            `retries`     int(8)       DEFAULT NULL COMMENT '重试次数',\n" +
                                     "            PRIMARY KEY (`id`)\n" +
                                     "        ) ENGINE = InnoDB\n" +
                                     "          DEFAULT CHARSET = utf8\n" +
                                     "          ROW_FORMAT = COMPACT;");
    }

    @Override
    public void insert(SendMessage message) {
        String sql = "INSERT INTO `transaction_message` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, ps -> setMessagePlaceholder(ps, message));
    }

    @Override
    public void insertBatch(List<SendMessage> messages) {

        String sql = "INSERT INTO `transaction_message` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                SendMessage message = messages.get(i);
                setMessagePlaceholder(ps, message);
            }

            @Override
            public int getBatchSize() {
                return messages.size();
            }
        });
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM `transaction_message` WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void deleteBatch(List<String> ids) {
        String sql = "DELETE FROM `transaction_message` WHERE id IN ";
        jdbcTemplate.execute(sql + ids.stream().map(id -> '\'' + id + '\'').collect(Collectors.joining(", ", "(", ")")));
    }

    @Override
    public void updateRetries(SendMessage message) {
        String sql = "UPDATE `transaction_message` SET retries = ? WHERE id = ?";
        jdbcTemplate.update(sql, message.getRetries(), message.getMessageId());
    }

    @Override
    public void updateRetriesBatch(List<SendMessage> messages) {

        String sql = "UPDATE `transaction_message` SET retries = ? WHERE id = ?";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                SendMessage message = messages.get(i);
                ps.setInt(1, message.getRetries());
                ps.setString(2, message.getMessageId());
            }

            @Override
            public int getBatchSize() {
                return messages.size();
            }
        });
    }

    void setMessagePlaceholder(PreparedStatement ps, SendMessage message) throws SQLException {
        ps.setString(1, message.getMessageId());
        ps.setString(2, message.getTopic());
        ps.setString(3, message.getType());
        ps.setString(4, message.getService());
        ps.setString(5, message.getContent());
        ps.setTimestamp(6, new Timestamp(message.getCreatedAt().getTime()));
        ps.setString(7, message.getPrimaryKey());
        ps.setString(8, message.getUsage().name());
        ps.setInt(9, message.getRetries());
    }
}
