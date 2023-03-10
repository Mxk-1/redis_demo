package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;
import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

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
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1. 校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2. 如果不符合，返回手机号格式错误
            return Result.fail("手机号格式错误");
        }

        // 3. 符合，生成验证码
        String code = RandomUtil.randomNumbers(6);

/*        // 4. 保存到session
        session.setAttribute("code", code);*/

        // 4. 保存到redis中
        // set key value ex 120
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);

        // 5. 发送验证码
        log.debug("发送短信验证码成功，验证码为：{}", code);

        // 返回OK
        return Result.ok();


    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        // 1. 校验手机号和验证码
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2. 如果不符合，返回手机号格式错误
            return Result.fail("手机号格式错误");
        }

        // 3. 从redis中获取校验验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);

        // session中获取验证码
//         3. 校验验证码
//        Object cacheCode = session.getAttribute("code");
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            // 不一致，报错
            return Result.fail("验证码错误");
        }

        // 4. 一致，根据手机号查询用户
        User user = query().eq("phone", phone).one();

        // 5. 判断用户是否存在
        if (user == null) {
            // 6. 不存在，创建新用户并保存
            user = createUserWithPhone(phone);
        }

        // 7. 保存用户信息到redis中
        // 7.1 生成一个token作为登陆令牌
        String token = UUID.randomUUID().toString(true);
        // 7.2 user对象转为hashMap存储
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(), CopyOptions.create().setIgnoreNullValue(true).setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        // 7.3 存储
        stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY + token, userMap);
        // 7.4 设置token有效期
        stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 保存数据到redis中
        // 返回token

        // 7. 保存用户信息到session中
//        session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));

        return Result.ok(token);
    }

    /**
     * @return com.hmdp.dto.Result
     * @description 用户登出
     * @author MXK
     * @date 2023/3/14 18:03
     */
    @Override
    public Result logout() {
        UserHolder.removeUser();
        return Result.ok();
    }

    @Override
    public Result queryUserById(Long userId) {
        User user = getById(userId);
        if (user == null) {
            return Result.ok();
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        // 返回
        return Result.ok(userDTO);
    }


    /***
     * @description 未注册用户注册
     * @param phone
     * @return com.hmdp.entity.User
     * @author MXK
     * @date 2023/3/2 21:04
     */
    private User createUserWithPhone(String phone) {
        // 1. 创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        // 2. 保存用户
        save(user);
        return user;
    }


}
