package com.xuecheng.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @author will
 * @version 1.0
 * @description 跨域过虑器
 * @date 2023/2/7 11:28
 */
@Configuration
public class GlobalCorsConfig {

    @Bean
    public CorsFilter getCorsFilter() {
        CorsConfiguration configuration = new CorsConfiguration();

        //添加哪些http方法可以跨域，比如：GET,Post，（多个方法中间以逗号分隔），*号表示所有
        configuration.addAllowedMethod("*");
        //添加允许哪个请求进行跨域，*表示所有,可以具体指定http://localhost:8601表示只允许http://localhost:8601/跨域
        configuration.addAllowedOrigin("*");
        //所有头信息全部放行
        configuration.addAllowedHeader("*");
        //允许跨域发送cookie
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        //拦截所有地址(所有请求过来都会响应该头信息)
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", configuration);
        return new CorsFilter(urlBasedCorsConfigurationSource);
    }
}
