package com.hmdp.config;

import com.hmdp.utils.LoginInterceptor;
import com.hmdp.utils.RefreshTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @author MXK
 * @version 1.0
 * @description TODO
 * @date 2023/3/2 19:48
 */

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        /***
         * @description 登录拦截器
         * @param registry
         * @return void
         * @author MXK
         * @date 2023/3/2 21:54
         */
        registry.addInterceptor(new LoginInterceptor()).excludePathPatterns("/user/code", "/user/login", "/blog/hot", "/shop/**", "/shop-type/**", "/upload/**", "/voucher/**").order(1);
        /***
         * @description 刷新拦截器
         * @param registry
         * @return void
         * @author MXK
         * @date 2023/3/2 21:54
         */
        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate)).addPathPatterns("/**").order(0);
    }
}

    