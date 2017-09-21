package com.paul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by liusonglin
 * Date:2017/9/20
 * Description:
 */
@EnableAutoConfiguration
@ComponentScan({"com.paul","org.decaywood.mapper"})
@EnableScheduling
public class AppApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }
}
