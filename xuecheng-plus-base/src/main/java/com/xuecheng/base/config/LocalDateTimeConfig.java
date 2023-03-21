package com.xuecheng.base.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author will
 * @version 1.0
 * @description 公共配置-时间参数管理
 * @date 2023/2/6 2:15
 */
@Configuration
public class LocalDateTimeConfig {


    /**
     * @return com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
     * @description 序列化内容LocalDateTime -> String：服务端返回给客户端内容
     * @author will
     * @date 2023/2/11 12:26
     */
    @Bean
    public LocalDateTimeSerializer localDateTimeSerializer() {
        return new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }


    /**
     * @return com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
     * @description 反序列化内容String -> LocalDateTime:客户端传入服务端数据
     * @author will
     * @date 2023/2/11 12:27
     */
    @Bean
    public LocalDateTimeDeserializer localDateTimeDeserializer() {
        return new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }


    /**
     * @param builder
     * @return com.fasterxml.jackson.databind.ObjectMapper
     * @description long转string避免精度损失
     * @author will
     * @date 2023/3/21 12:48
     */
    @Bean
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        //忽略value为null 时 key的输出
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(module);
        return objectMapper;
    }


    /**
     * @return org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
     * @description 配置序列化和反序列化
     * @author will
     * @date 2023/2/11 12:28
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            builder.serializerByType(LocalDateTime.class, localDateTimeSerializer());
            builder.deserializerByType(LocalDateTime.class, localDateTimeDeserializer());
        };
    }

}
