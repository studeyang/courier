package com.github.open.courier.client.producer.transaction;

import java.util.List;

import com.github.open.courier.core.transport.SendMessage;

public interface TransactionMessageMapper {

    void createTableIfNotExists();

    void insert(SendMessage message);

    void insertBatch(List<SendMessage> messages);

    void delete(String id);

    void deleteBatch(List<String> ids);

    void updateRetries(SendMessage message);

    void updateRetriesBatch(List<SendMessage> messages);
}
