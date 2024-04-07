package com.github.open.courier.consumer;

import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import io.github.open.toolkit.config.annotation.PrepareConfigurations;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Courier
 */
@SpringBootApplication
@PrepareConfigurations({"__common_message_client2_", "__common_feign_client_"})
@NacosPropertySource(groupId = "${nacos.config.group}", dataId = "${spring.application.name}.yml", first = true)
@NacosPropertySource(groupId = "${nacos.config.group}", dataId = "courier_sharding_database.yml")
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }

}
