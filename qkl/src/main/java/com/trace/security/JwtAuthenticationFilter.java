package com.trace.security;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.trace.common.Result;
import com.trace.common.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * JWT认证拦截器
 * 拦截需要登录的接口，验证Token有效性
 */
@Slf4j
@Component
public class JwtAuthenticationFilter implements HandlerInterceptor {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = resolveToken(request);
        if (StrUtil.isBlank(token)) {
            writeError(response, ResultCode.UNAUTHORIZED);
            return false;
        }

        // 校验Token
        if (!jwtTokenProvider.validateToken(token)) {
            writeError(response, ResultCode.UNAUTHORIZED);
            return false;
        }

        // Token有效，把用户信息放到request属性里方便后续使用
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        String username = jwtTokenProvider.getUsernameFromToken(token);
        String role = jwtTokenProvider.getRoleFromToken(token);

        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("role", role);

        return true;
    }

    /**
     * 从请求头里提取Token
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StrUtil.isNotBlank(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 返回错误信息
     */
    private void writeError(HttpServletResponse response, ResultCode resultCode) throws IOException {
        response.setStatus(200); // HTTP状态统一200，业务状态码在body里
        response.setContentType("application/json;charset=UTF-8");
        Result<?> result = Result.fail(resultCode);
        response.getWriter().write(JSON.toJSONString(result));
    }
}
