package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * @return com.hmdp.dto.Result
     * @description 首页商铺分类页面展示缓存到redis中
     * @author MXK
     * @date 2023/3/5 01:22
     */
    /* @Override
    public Result queryAll() {

        String key = "cache:typeList";

        // 1. 从redis查询店铺类型缓存
        List<String> shopTypeList = new ArrayList<>();
        shopTypeList = stringRedisTemplate.opsForList().range(key, 0, -1);

        // 2. 判断是否存在
        // 3. 存在直接返回
        if (!shopTypeList.isEmpty()) {
            List<ShopType> typeList = new ArrayList<>();
            for (String s : shopTypeList) {
                ShopType shopType = JSONUtil.toBean(s, ShopType.class);
                typeList.add(shopType);
            }
        }

        // 4. 不存在，查询所有数据
        List<ShopType> typeList = query().orderByAsc("sort").list();

        // 5. 不存在，返回错误
        if (typeList.isEmpty()) {
            return Result.fail("不存在分类");
        }
        // 6. 存在，写入redis
        for (ShopType shopType : typeList) {
            String s = JSONUtil.toJsonStr(shopType);
            shopTypeList.add(s);
        }

        stringRedisTemplate.opsForList().rightPushAll(key, shopTypeList);

        // 7. 返回
        return Result.ok(typeList);
    } */
    @Override
    public Result getList() {

        String key = "cache:typeList";

        // 在Redis中查询
        List<String> shopTypeList = new ArrayList<>();
        shopTypeList = stringRedisTemplate.opsForList().range(key, 0, -1);

        // 判断是否在缓存中
        // 在，返回
        if (!shopTypeList.isEmpty()) {
            List<ShopType> typeList = new ArrayList<>();
            for (String s : shopTypeList) {
                ShopType shopType = JSONUtil.toBean(s, ShopType.class);
                typeList.add(shopType);
            }
            return Result.ok(typeList);
        }

        // 没中，数据库中查询
        List<ShopType> typeList = query().orderByAsc("sort").list();

        // 不存在直接返回错误
        if (typeList.isEmpty()) {
            return Result.fail("不存在分类");
        }

        for (ShopType shopType : typeList) {
            String s = JSONUtil.toJsonStr(shopType);

            shopTypeList.add(s);
        }
        // 存在直接添加进缓存
        stringRedisTemplate.opsForList().rightPushAll(key, shopTypeList);
        // 设置缓存到期时间
        stringRedisTemplate.expire(key, 30L, TimeUnit.MINUTES);

        return Result.ok(typeList);
    }
}
