package com.jeckchen.humanverificationsystem.utils;

import jakarta.servlet.http.HttpServletRequest;

public class IpUtil {

    public static final String UNKNOWN = "unknown";
    public static final String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
    public static final String PROXY_CLIENT_IP = "Proxy-Client-IP";
    public static final String X_FORWARDED_FOR = "x-forwarded-for";
    public static final String IPV4_LOCAL = "127.0.0.1";
    public static final String IPV6_LOCAL = "0:0:0:0:0:0:0:1";

    /**
     * 获取客户端IP地址
     *
     * @param request HttpServletRequest对象
     * @return 客户端IP地址
     */
    public static String getIpAddr(HttpServletRequest request) {
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
