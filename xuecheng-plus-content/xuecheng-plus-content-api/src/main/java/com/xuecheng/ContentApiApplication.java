package com.xuecheng;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


/**
 * @author will
 * @version 1.0
 * @description controller层启动类
 * @date 2023/2/11 15:32
 */
@EnableSwagger2Doc
@SpringBootApplication
@EnableFeignClients(basePackages = {"com.xuecheng.content.feignclient"})
public class ContentApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContentApiApplication.class, args);
    }

}
