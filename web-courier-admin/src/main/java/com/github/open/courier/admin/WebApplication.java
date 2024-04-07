package com.github.open.courier.admin;

import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import com.github.open.courier.admin.client.AdminClient;
import com.github.open.courier.admin.client.ProducerClient;
import io.github.open.toolkit.config.annotation.PrepareConfigurations;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author Courier
 */
@SpringBootApplication
@EnableFeignClients(clients = {AdminClient.class, ProducerClient.class})
@PrepareConfigurations({"__common_feign_client_.yml"})
@NacosPropertySource(groupId = "${nacos.config.group}", dataId = "${spring.application.name}.yml", first = true)
public class WebApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(WebApplication.class, args);
    }
}
