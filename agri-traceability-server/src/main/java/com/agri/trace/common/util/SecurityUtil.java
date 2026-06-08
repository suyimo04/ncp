package com.agri.trace.common.util;

import com.agri.trace.common.exception.BusinessException;
import com.agri.trace.security.CurrentUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;

public class SecurityUtil {
    private SecurityUtil() {
    }

    public static CurrentUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUser user)) {
            throw new BusinessException(401, "登录状态已失效");
        }
        return user;
    }

    public static Long userId() {
        return currentUser().getUserId();
    }

    public static boolean hasRole(String roleCode) {
        List<String> roles = currentUser().getRoles();
        return roles != null && roles.contains(roleCode);
    }

    public static boolean hasAnyRole(String... roleCodes) {
        List<String> roles = currentUser().getRoles();
        return roles != null && Arrays.stream(roleCodes).anyMatch(roles::contains);
    }

    public static boolean isProducerOnly() {
        return hasRole("PRODUCER") && !hasAnyRole("ADMIN", "REGULATOR");
    }

    public static void requireRole(String... roleCodes) {
        if (!hasAnyRole(roleCodes)) {
            throw new BusinessException(403, "无权限访问");
        }
    }
}
