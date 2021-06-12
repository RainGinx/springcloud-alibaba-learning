package com.chb.learning.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author caihongbin
 * @date 2021/6/12 16:25
 */
@SpringBootApplication
@EnableFeignClients(basePackages = {"com.chb.learning"})
@ComponentScan(basePackages = {"com.chb.learning"})
@MapperScan("com.chb.learning.admin.mapper")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }
}
