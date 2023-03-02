package com.jedis.test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;

/**
 * @author MXK
 * @version 1.0
 * @description TODO
 * @date 2023/2/27 23:37
 */


public class TestJedis {
    private Jedis jedis;

//    @BeforeEach
//    @Test
//    void setUp() {
//        // 1. 建立连接
//        jedis = new Jedis("192.168.101.135", 6379);
//        jedis.auth("mxkmxk");
//        jedis.select(0);
//    }

    @BeforeEach
    @Test
    void setUp2() {
        // 1. 建立连接
        jedis = JedisConnectFactory.getJedis();
        jedis.auth("mxkmxk");
        jedis.select(0);
    }

    @Test
    void testString() {
        // 存入数据
        String result = jedis.set("name", "虎哥");
        System.out.println("result = " + result);

        // 取数据
        String name = jedis.get("name");
        System.out.println("name = " + name);
    }

    @Test
    void testHash() {
        // 插入hash数据
        jedis.hset("user:1", "name", "mxk");
        jedis.hset("user:1", "age", "25");

        // 获取
        Map<String, String> user1 = jedis.hgetAll("user:1");
        System.out.println(user1);
    }

    // 释放资源
    @AfterEach
    void tearDown() {
        if (jedis != null) {
            jedis.close();
            System.out.println("jedis close");
        }
    }
}

class JedisConnectFactory {
    private static final JedisPool jedisPool;

    static {
        // 配置连接池
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(8);
        poolConfig.setMaxIdle(8);
        poolConfig.setMinIdle(0);
        poolConfig.setMaxWaitMillis(1000);
        // 创建连接池对象
        jedisPool = new JedisPool(poolConfig, "192.168.101.135", 6379, 1000, "mxkmxk");
    }

    public static Jedis getJedis() {
        return jedisPool.getResource();
    }
}
    