package com.jeckchen.humanverificationsystem.config.rateLimit;

import com.jeckchen.humanverificationsystem.utils.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class RateLimitAspect {

    // 存储每个IP的请求计数
    private final ConcurrentHashMap<String, Long> ipRequestCount = new ConcurrentHashMap<>();

    // 存储全局的请求计数
    private long globalRequestCount = 0;

    // 上次重置计数的时间戳
    private long lastResetTime = System.currentTimeMillis();

    @Before("@annotation(rateLimit)")
    public void rateLimit(RateLimit rateLimit) throws Exception {
        HttpServletRequest request = getRequest();
        String ip = getClientIp(request);

        int ipLimit = rateLimit.ipLimit();
        int globalLimit = rateLimit.globalLimit();

        // 检查是否需要重置计数
        resetIfNeeded();

        // 检查IP限流
        if (isIpRateLimited(ip, ipLimit)) {
            throw new Exception("IP rate limit exceeded");
        }

        // 检查全局限流
        if (isGlobalRateLimited(globalLimit)) {
            throw new Exception("Global rate limit exceeded");
        }

        // 增加计数
        ipRequestCount.put(ip, ipRequestCount.getOrDefault(ip, 0L) + 1);
        globalRequestCount++;
    }

    private boolean isIpRateLimited(String ip, int limit) {
        long count = ipRequestCount.getOrDefault(ip, 0L);
        return count >= limit;
    }

    private boolean isGlobalRateLimited(int limit) {
        return globalRequestCount >= limit;
    }

    private void resetIfNeeded() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastResetTime > 1000) { // 超过1秒，重置计数
            ipRequestCount.clear();
            globalRequestCount = 0;
            lastResetTime = currentTime;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        return IpUtil.getIpAddr(request);
    }

    private HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }
}