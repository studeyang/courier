package com.github.open.courier.admin.config;

import io.github.open.toolkit.web.GenericWebMvcConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import java.util.Map;

@Configuration
public class WebMvcConfig extends GenericWebMvcConfig {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }

    @Bean
    public FreeMarkerViewResolver freeMarkerViewResolver(IcecResourcesBean icecResourcesBean) {
        FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
        resolver.setPrefix("");
        resolver.setSuffix(".ftl");
        resolver.setContentType("text/html; charset=UTF-8");
        resolver.setRequestContextAttribute("request");
        resolver.setAttributesMap(icecResourcesBean.getResources());
        resolver.setRedirectHttp10Compatible(false);
        return resolver;
    }

    @Data
    @Component
    @ConfigurationProperties(prefix = "icec")
    public static class IcecResourcesBean {

        private Map<String, String> resources;
    }
}
