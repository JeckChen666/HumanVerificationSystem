package com.jeckchen.humanverificationsystem.config;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.io.BufferedReader;
import java.util.Map;

/**
 * web的调用时间统计拦截器
 *
 * @author Sakura
 */
@Slf4j
@Component
public class WebInvokeTimeInterceptor implements HandlerInterceptor {

    public static final String UNKNOWN = "unknown";
    public static final String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
    public static final String PROXY_CLIENT_IP = "Proxy-Client-IP";
    public static final String X_FORWARDED_FOR = "x-forwarded-for";
    public static final String IPV4_LOCAL = "127.0.0.1";
    public static final String IPV6_LOCAL = "0:0:0:0:0:0:0:1";
    private final TransmittableThreadLocal<StopWatch> invokeTimeTL = new TransmittableThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = getIpAddr(request);
        String url = request.getMethod() + " " + request.getRequestURI();

        // 打印请求参数
        if (isJsonRequest(request)) {
            String jsonParam = "";
            if (request instanceof RepeatedlyRequestWrapper) {
                BufferedReader reader = request.getReader();
                jsonParam = IoUtil.read(reader);
            }
            log.info("[{}]开始请求 => URL[{}],参数类型[json],参数:[{}]", ip, url, jsonParam);
        } else {
            Map<String, String[]> parameterMap = request.getParameterMap();
            if (MapUtil.isNotEmpty(parameterMap)) {
                String parameters = "";
                try {
                    // 将 parameterMap 转换为 JSON 字符串
                    parameters = JSONUtil.toJsonStr(parameterMap);
                }catch (Exception e){
                    log.error("preHandle toJsonStr Error", e);
                }
                log.info("[{}]开始请求 => URL[{}],参数类型[param],参数:[{}]", ip, url, parameters);
            } else {
                log.info("[{}]开始请求 => URL[{}],无参数", ip, url);
            }
        }

        StopWatch stopWatch = new StopWatch();
        invokeTimeTL.set(stopWatch);
        stopWatch.start();

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            String ip = getIpAddr(request);
            StopWatch stopWatch = invokeTimeTL.get();
            stopWatch.stop();
            log.info("[{}]结束请求 => URL[{}],耗时:[{}]毫秒", ip, request.getMethod() + " " + request.getRequestURI(), stopWatch.getTime());
            invokeTimeTL.remove();
        } catch (RuntimeException e) {
            log.error("afterCompletion error", e);
        }
    }

    /**
     * 判断本次请求的数据类型是否为json
     *
     * @param request request
     * @return boolean
     */
    private boolean isJsonRequest(HttpServletRequest request) {
        // 获取请求的内容类型
        String contentType = request.getContentType();
        if (contentType != null) {
            // 判断内容类型是否以json开头
            return StringUtils.startsWithIgnoreCase(contentType, MediaType.APPLICATION_JSON_VALUE);
        }
        // 如果内容类型为空，则返回false
        return false;
    }


    /**
     * 获取客户端IP地址
     *
     * @param request HttpServletRequest对象
     * @return 客户端IP地址
     */
    private String getIpAddr(HttpServletRequest request) {
        // 获取x-forwarded-for头信息
        String ip = request.getHeader(X_FORWARDED_FOR);
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            // 获取Proxy-Client-IP头信息
            ip = request.getHeader(PROXY_CLIENT_IP);
        }
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            // 获取WL-Proxy-Client-IP头信息
            ip = request.getHeader(WL_PROXY_CLIENT_IP);
        }
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            // 获取远程地址
            ip = request.getRemoteAddr();
        }
        if (IPV6_LOCAL.equals(ip)) {
            // 将IPv6地址"0:0:0:0:0:0:0:1"替换为IPv4地址"127.0.0.1"
            ip = IPV4_LOCAL;
        }
        if (ip.split(",").length > 1) {
            // 取第一个IP地址
            ip = ip.split(",")[0];
        }
        return ip;
    }

}
