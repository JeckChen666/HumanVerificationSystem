package com.jeckchen.humanverificationsystem.config.rateLimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    int ipLimit() default 10; // 每秒每个IP的最大访问次数
    int globalLimit() default 100; // 每秒所有请求的最大访问次数
}