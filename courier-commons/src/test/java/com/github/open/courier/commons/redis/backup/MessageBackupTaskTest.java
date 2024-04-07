package com.github.open.courier.commons.redis.backup;

import lombok.Data;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.Optional;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 1.0 2022/6/23
 */
public class MessageBackupTaskTest {

    @Test
    public void test_npe() {
        Message message = new Message();
        long delay = Optional.ofNullable(message.getCreateAt())
                .map(date -> System.currentTimeMillis() - date.getTime())
                .orElse(-1L);
        Assert.assertEquals(-1, delay);
    }

    @Data
    private static class Message {
        private Date createAt;
    }

}