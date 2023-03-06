package com.hmdp.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author MXK
 * @version 1.0
 * @description 设置过期时间
 * @date 2023/3/6 17:43
 */
@Data
public class RedisData {

    private LocalDateTime expireTime;

    private Object data;

}

    