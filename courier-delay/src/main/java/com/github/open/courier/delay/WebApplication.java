package com.github.open.courier.delay;

import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import com.github.open.courier.commons.support.CourierServerProperties;
import com.github.open.courier.delay.client.ManagementClient;
import io.github.open.toolkit.config.annotation.PrepareConfigurations;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableFeignClients(clients = {ManagementClient.class})
@EnableConfigurationProperties(CourierServerProperties.class)
@PrepareConfigurations({"__common_message_client2_.yml", "__common_feign_client_.yml"})
@NacosPropertySource(groupId = "${nacos.config.group}", dataId = "courier_sharding_database.yml")
@NacosPropertySource(groupId = "${nacos.config.group}", dataId = "${spring.application.name}.yml", first = true)
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}
