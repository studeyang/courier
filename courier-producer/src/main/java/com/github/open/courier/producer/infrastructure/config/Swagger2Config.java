package com.github.open.courier.producer.infrastructure.config;

import com.github.open.courier.commons.support.CourierServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Optional;

/**
 * Swagger配置
 */
@Configuration
@EnableSwagger2
public class Swagger2Config {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .enableUrlTemplating(true)
                .groupName("courier-producer(auto)")
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        String version = Optional.ofNullable(CourierServer.class.getPackage())
                .map(Package::getImplementationVersion)
                .orElse("1.0.0-SNAPSHOT");
        return new ApiInfoBuilder()
                .title("courier-producer")
                .description("API 接口说明[courier-producer]")
                .version(version)
                .contact(new Contact("消息总线", "", ""))
                .build();
    }

}
