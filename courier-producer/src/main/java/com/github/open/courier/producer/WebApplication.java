package com.github.open.courier.producer;

import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import io.github.open.toolkit.config.annotation.PrepareConfigurations;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@PrepareConfigurations({"__common_message_client2_.yml", "__common_feign_client_.yml"})
@NacosPropertySource(groupId = "${nacos.config.group}", dataId = "${spring.application.name}.yml", first = true)
@NacosPropertySource(groupId = "${nacos.config.group}", dataId = "courier_sharding_database.yml")
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}
