package com.hmdp.utils;

/**
 * @author MXK
 * @version 1.0
 * @description TODO
 * @date 2023/3/8 10:38
 */

public interface ILock {

    /**
     * @param timeoutSec
     * @return boolean
     * @description 尝试获取锁
     * @author MXK
     * @date 2023/3/8 10:38
     */
    boolean tryLock(long timeoutSec);

    /**
     * @return void
     * @description 释放锁
     * @author MXK
     * @date 2023/3/8 10:38
     */
    void unlock();
}
