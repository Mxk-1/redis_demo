package com.hmdp.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author MXK
 * @version 1.0
 * @description Redisson配置
 * @date 2023/3/9 10:01
 */
@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient() {
        // 配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.101.135:6379").setPassword("mxkmxk");
        // 创建RedissonClient对象
        return Redisson.create(config);
    }
}

    