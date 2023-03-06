package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.RedisData;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.CacheClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private CacheClient cacheClient;

    /**
     * @param id
     * @return com.hmdp.dto.Result
     * @description 根据id查询店铺信息
     * @author MXK
     * @date 2023/3/6 16:04
     */
    @Override
    public Result queryById(Long id) {

        // 1. 缓存穿透
        // Shop shop = queryWithPassThrough(id);
        // Shop shop = cacheClient.queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, id2 -> getById(id2), CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 互斥锁解决缓存击穿
        // Shop shop = queryWithMutex(id);
        Shop shop = cacheClient.queryWithLogicalExpire(CACHE_SHOP_KEY, id, Shop.class, id2 -> getById(id2), 20L, TimeUnit.MINUTES);

        // 逻辑过期解决缓存击穿
        // Shop shop = queryWithLogicalExpire(id);

        if (shop == null) {
            return Result.fail("店铺不存在");
        }

        // 7. 返回
        return Result.ok(shop);
    }

    /**
     * @param id
     * @return com.hmdp.entity.Shop
     * @description 互斥锁解决缓存击穿
     * @author MXK
     * @date 2023/3/6 16:03
     */
    public Shop queryWithMutex(Long id) {
        String key = CACHE_SHOP_KEY + id;
        Shop shop = null;

        try {
            // 1. 从redis查询商户缓存
            String shopJson = stringRedisTemplate.opsForValue().get(key);

            // 2. 判断是否存在
            if (StrUtil.isNotBlank(shopJson)) {
                // 3. 存在直接返回
                return JSONUtil.toBean(shopJson, Shop.class);
            }

            // 判断命中的是否是空值
            if (shopJson != null) {
                return null;
            }
            // if ("".equals(shopJson)) {
            //     return Result.fail("店铺不存在");
            // }

            /* ================================================================================== */
            // 4. 实现缓存重建
            // 4.1 获取互斥锁
            boolean isLock = tryLock(LOCK_SHOP_KEY + id);

            // 4.2 判断是否获取成功
            if (!isLock) {
                // 4.3 失败，则休眠并重试
                // Thread.sleep(50);

                // 模拟休眠
                Thread.sleep(200);
                queryWithMutex(id);
            }

            // 4.4 成功，根据id查询数据库
            shop = getById(id);
            // 5. 不存在，返回错误
            if (shop == null) {

                //  将空值写入redis
                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);

                // 返回信息
                return null;
            }
            // 6. 存在，写入redis
            // 6.1 设置过期时间
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 7. 释放互斥锁
            unlock(LOCK_SHOP_KEY + id);
        }

        // 8. 返回
        return shop;
    }

    /**
     * @description 线程池
     * @param null
     * @return
     * @author MXK
     * @date 2023/3/6 17:55
     */
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public Shop queryWithLogicalExpire(Long id) {
        String key = CACHE_SHOP_KEY + id;
        // 1.从redis查询商铺缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2.判断是否存在
        if (StrUtil.isBlank(json)) {
            // 3.存在，直接返回
            return null;
        }
        // 4.命中，需要先把json反序列化为对象
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        Shop shop = JSONUtil.toBean((JSONObject) redisData.getData(), Shop.class);
        LocalDateTime expireTime = redisData.getExpireTime();
        // 5.判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 5.1.未过期，直接返回店铺信息
            return shop;
        }
        // 5.2.已过期，需要缓存重建
        // 6.缓存重建
        // 6.1.获取互斥锁
        String lockKey = LOCK_SHOP_KEY + id;
        boolean isLock = tryLock(lockKey);
        // 6.2.判断是否获取锁成功
        if (isLock) {
            CACHE_REBUILD_EXECUTOR.submit(() -> {

                try {
                    // 重建缓存
                    this.saveShop2Redis(id, 20L);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    unlock(lockKey);
                }
            });
        }
        // 6.4.返回过期的商铺信息
        return shop;
    }

    /**
     * @param id
     * @return com.hmdp.entity.Shop
     * @description 缓存穿透，将空值写入redis
     * @author MXK
     * @date 2023/3/6 16:01
     */
    public Shop queryWithPassThrough(Long id) {
        String key = CACHE_SHOP_KEY + id;

        // 1. 从redis查询商户缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);

        // 2. 判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            // 3. 存在直接返回
            return JSONUtil.toBean(shopJson, Shop.class);
        }

        // 判断命中的是否是空值
        if (shopJson != null) {
            return null;

        }
        // if ("".equals(shopJson)) {
        //     return Result.fail("店铺不存在");
        // }

        // 4. 不存在，根据id查询数据库
        Shop shop = getById(id);
        // 5. 不存在，返回错误
        if (shop == null) {

            //  将空值写入redis
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);

            // 返回信息
            return null;
        }
        // 6. 存在，写入redis
        // 6.1 设置过期时间
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 7. 返回
        return shop;
    }

    /**
     * @param key
     * @return boolean
     * @description 获取锁
     * @author MXK
     * @date 2023/3/6 15:58
     */
    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    /**
     * @param key
     * @return void
     * @description 释放锁
     * @author MXK
     * @date 2023/3/6 15:59
     */
    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }

    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺id为空");
        }

        // 1. 更新数据库
        updateById(shop);

        // 2. 删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id);
        return Result.ok();
    }


    @Override
    public void saveShop2Redis(Long id, Long expireSeconds) {
        // 1. 查询店铺数据
        Shop shop = getById(id);

        // 2. 封装逻辑过期时间
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));

        // 3. 写入redis
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(redisData));
    }
}
