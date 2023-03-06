package com.hmdp.test;

import com.hmdp.service.IShopService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author MXK
 * @version 1.0
 * @description 测试
 * @date 2023/3/6 17:48
 */
@SpringBootTest
public class HmdpTest {

    @Resource
    public IShopService shopService;

    @Test
    void testSaveShop() {
        shopService.saveShop2Redis(1L, 10L);
    }
}

    