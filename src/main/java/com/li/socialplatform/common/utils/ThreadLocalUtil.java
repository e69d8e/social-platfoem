package com.li.socialplatform.common.utils;

import org.springframework.stereotype.Component;

@Component
public class ThreadLocalUtil {
    // 线程本地存储
    private static final ThreadLocal<Object> THREAD_LOCAL = new ThreadLocal<>();
    // 设置值
    public void set(Object value){
        THREAD_LOCAL.set(value);
    }
    // 获取值
    public Object get(){
        return THREAD_LOCAL.get();
    }
    // 移除 防止内存泄漏
    public void remove(){
        THREAD_LOCAL.remove();
    }
}
