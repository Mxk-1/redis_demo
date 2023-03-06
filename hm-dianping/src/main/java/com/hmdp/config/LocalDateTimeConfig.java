package com.hmdp.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author MXK
 * @version 1.0
 * @description 时间序列化器
 * @date 2023/3/6 10:37
 */

@Configuration
public class LocalDateTimeConfig {

    @Value("${spring.jackson.date-format:yyyy-MM-dd HH:mm:ss}")
    private String pattern;

    /**
     * @return com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
     * @description 序列化内容
     * LocalDateTime -> String
     * 服务端返回给客户端
     * @author MXK
     * @date 2023/3/6 10:39
     */
    @Bean
    public LocalDateTimeSerializer localDateTimeSerializer() {
        return new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * @return com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
     * @description 反序列化内容
     * * String -> LocalDateTime
     * * 客户端传入服务端数据
     * @author MXK
     * @date 2023/3/6 10:41
     */
    @Bean
    public LocalDateTimeDeserializer localDateTimeDeserializer() {
        return new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(pattern));
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            builder.serializerByType(LocalDateTime.class, localDateTimeSerializer());
            builder.deserializerByType(LocalDateTime.class, localDateTimeDeserializer());
        };
    }

}

    