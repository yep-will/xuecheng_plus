package com.xuecheng;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * @author will
 * @version 1.0
 * @description controller层启动类
 * @date 2023/2/11 15:32
 */
@EnableSwagger2Doc
@SpringBootApplication
public class ContentApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContentApiApplication.class, args);
    }

}
