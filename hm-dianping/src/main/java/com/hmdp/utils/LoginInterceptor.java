package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.LOGIN_USER_KEY;
import static com.hmdp.utils.RedisConstants.LOGIN_USER_TTL;

/**
 * @author MXK
 * @version 1.0
 * @description TODO
 * @date 2023/3/2 19:42
 */

public class LoginInterceptor implements HandlerInterceptor {


    private StringRedisTemplate stringRedisTemplate;

//    public LoginInterceptor(StringRedisTemplate stringRedisTemplate) {
//        this.stringRedisTemplate = stringRedisTemplate;
//    }

    /***
     * @description redis token
     * @param request
     * @param response
     * @param handler
     * @return boolean
     * @author MXK
     * @date 2023/3/2 21:17
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        // TODO 1. 获取token
////        HttpSession session = request.getSession();
//        String token = request.getHeader("authorization");
//        if (StrUtil.isBlank(token)) {
//            response.setStatus(401);
//            return false;
//        }
//        // TODO 2. 基于token获取redis中的用户
//        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(LOGIN_USER_KEY + token);
//
////        Object user = session.getAttribute("user");
//        // 3. 判断用户是否存在
//        if (userMap.isEmpty()) {
//            // 4. 不存在，返回401状态码
//            response.setStatus(401);
//            return false;
//
//        }
//
//        // TODO 5. 将查询到的Hash数据转为UserDTO对象
//        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
//
//        // TODO 6. 存在，保存用户到ThreadLocal
//        UserHolder.saveUser((UserDTO) userDTO);
//
//        // TODO 7. 刷新token有效期
//        stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.MINUTES);
//        // TODO 8. 放行

        // 1. 判断是否需要去拦截（ThreadLocal是否有用户）
        if (UserHolder.getUser() == null) {
            // 没有，设置拦截码
            response.setStatus(401);
            return false;
        }
        // 有用户，则放行
        return true;
    }

    /***
     * @description session登录拦截器
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @return void
     * @author MXK
     * @date 2023/3/2 21:17
     */
/*    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取session
        HttpSession session = request.getSession();
        // 2. 获取session中的用户
        Object user = session.getAttribute("user");
        // 3. 判断用户是否存在
        if (user == null) {
            // 4. 不存在，返回401状态码
            response.setStatus(401);
            return false;
        }

        // 5. 存在，保存用户到ThreadLocal
        UserHolder.saveUser((UserDTO) user);
//        UserHolder.saveUser((User) user);

        // 6. 放行
        return true;
    }*/
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}

    