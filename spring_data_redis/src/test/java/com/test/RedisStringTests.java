package com.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author MXK
 * @version 1.0
 * @description TODO
 * @date 2023/3/2 17:36
 */

@SpringBootTest
class RedisStringTests {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void testString() {
        // 写入一条数据
        stringRedisTemplate.opsForValue().set("verify:phone:17777777777", "123456");

        // 获取String数据
        String name = stringRedisTemplate.opsForValue().get("name");
        System.out.println(name);
    }
}

    