package com.github.open.courier.example;

import com.github.open.courier.annotation.EnableMessage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author Courier
 */
@EnableMessage
@SpringBootApplication
public class WebApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(WebApplication.class, args);
        System.out.println(context.getEnvironment().getProperty("courier.agent.url"));
    }
}
