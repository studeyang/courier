package com.github.open.courier.producer.service;

import com.github.open.courier.commons.redis.RedisClient;
import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.converter.MessageJsonConverter;
import com.github.open.courier.producer.restful.PushService;
import com.github.open.courier.core.support.Wrapper;
import com.github.open.courier.core.transport.PausePushRequest;
import com.github.open.courier.core.transport.ResumePushRequest;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PushService.class, Wrapper.class}, properties = "courier.topic-prefix=alpha")
public class PushServiceTest {

    private MockMvc mockMvc;

    @InjectMocks
    private PushService pushService;

    @MockBean
    private Producer<String, String> producer;

    @MockBean
    private RedisClient redisClient;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(pushService)
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(),
                        new StringHttpMessageConverter(StandardCharsets.UTF_8)
                )
                .build();
    }

    @Test
    public void pause() throws Exception {

        when(producer.send(any())).thenReturn(new TestFuture());

        doNothing().when(redisClient).add(anyString(), anyString());

        mockMvc.perform(
                MockMvcRequestBuilders.post(URLConstant.PRODUCER_PAUSE_PUSH)
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(MessageJsonConverter.toJson(new PausePushRequest()))
        ).andExpect(status().is2xxSuccessful());
    }

    @Test
    public void resume() throws Exception {

        when(producer.send(any())).thenReturn(new TestFuture());

        doNothing().when(redisClient).remove(anyString(), anyString());

        mockMvc.perform(
                post(URLConstant.PRODUCER_RESUME_PUSH)
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(MessageJsonConverter.toJson(new ResumePushRequest()))
        ).andExpect(status().is2xxSuccessful());
    }

    private static class TestFuture implements Future<RecordMetadata> {

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public RecordMetadata get() {
            return null;
        }

        @Override
        public RecordMetadata get(long timeout, TimeUnit unit) throws InterruptedException {
            unit.sleep(timeout);
            return null;
        }
    }

}