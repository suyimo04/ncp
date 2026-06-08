package com.trace.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 请求上下文工具类
 * 方便在Service层获取当前登录用户信息
 */
public class RequestContext {

    /**
     * 获取当前请求对象
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        return attrs.getRequest();
    }

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        HttpServletRequest request = getRequest();
        if (request == null) return null;
        Object userId = request.getAttribute("userId");
        return userId == null ? null : (Long) userId;
    }

    /**
     * 获取当前登录用户名
     */
    public static String getCurrentUsername() {
        HttpServletRequest request = getRequest();
        if (request == null) return null;
        return (String) request.getAttribute("username");
    }

    /**
     * 获取当前登录用户角色
     */
    public static String getCurrentRole() {
        HttpServletRequest request = getRequest();
        if (request == null) return null;
        return (String) request.getAttribute("role");
    }

    /**
     * 获取客户端IP
     */
    public static String getClientIp() {
        HttpServletRequest request = getRequest();
        if (request == null) return "unknown";
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
