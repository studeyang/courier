package com.github.open.courier.commons.configuration;

import com.github.open.courier.core.support.CourierContext;
import com.github.open.courier.commons.support.CourierServerProperties;
import com.github.open.courier.core.support.Wrapper;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Courier 服务端基础 Bean 装配
 *
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/6/23
 */
@EnableConfigurationProperties(CourierServerProperties.class)
public class CourierServerBaseAutoConfiguration {

    @Bean("courierServerRestTemplate")
    @Primary
    public RestTemplate courierServerRestTemplate(CourierServerProperties properties) {
        HttpClient httpClient = initHttpClient(properties);
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
    }

    private HttpClient initHttpClient(CourierServerProperties properties) {
        long readTimeout = properties.getRestTemplate().getReadTimeoutMs();
        long connectTimeout = properties.getRestTemplate().getConnectTimeoutMs();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        connectionManager.setMaxTotal(properties.getRestTemplate().getPool().getMaxTotal());
        connectionManager.setDefaultMaxPerRoute(properties.getRestTemplate().getPool().getMaxPerRoute());
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout((int) readTimeout) // 服务器返回数据(response)的时间，超过该时间抛出read timeout
                .setConnectTimeout((int) connectTimeout) // 连接上服务器(握手成功)的时间，超出该时间抛出connect timeout
                // 从连接池中获取连接的超时时间，超过该时间未拿到可用连接，会抛出
                // org.apache.http.conn.ConnectionPoolTimeoutException: Timeout waiting for connection from pool
                .setConnectionRequestTimeout(properties.getRestTemplate().getPool().getConnectionRequestTimeout())
                .build();
        return HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionManager)
                .build();
    }

    @Bean
    public CourierContext courierContext() {
        return new CourierContext();
    }

    @Bean
    @ConditionalOnProperty("courier.topic-prefix")
    public Wrapper wrapper() {
        return new Wrapper();
    }

}
