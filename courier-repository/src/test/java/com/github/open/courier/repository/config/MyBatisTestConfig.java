package com.github.open.courier.repository.config;

import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@MapperScan("com.github.open.courier.repository.mapper")
@Import({DataSourceAutoConfiguration.class, MybatisAutoConfiguration.class})
public class MyBatisTestConfig {

}