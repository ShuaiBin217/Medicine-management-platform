package com.wms.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录人工具类。
 *
 * <p>登录时 {@link com.wms.controller.UserController#login} 把 roleId 写进权限名
 * （ROLE_0 / ROLE_1 / ROLE_2），把用户 id 写进 Authentication.details。
 * 业务代码取角色、取操作人一律走这里，不再从请求体里读，避免前端伪造。</p>
 */
public final class AuthUtils {

    /** 登录时角色权限名前缀，见 UserController#login */
    private static final String ROLE_PREFIX = "ROLE_";

    private AuthUtils() {
    }

    public static Authentication currentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 当前登录人角色：0 超级管理员，1 管理员，2 取药/查询人员。
     *
     * @return 未登录或权限名非法时返回 null
     */
    public static Integer currentRole() {
        Authentication auth = currentAuthentication();
        if (auth == null) {
            return null;
        }
        for (GrantedAuthority authority : auth.getAuthorities()) {
            String name = authority == null ? null : authority.getAuthority();
            if (name != null && name.startsWith(ROLE_PREFIX)) {
                try {
                    return Integer.valueOf(name.substring(ROLE_PREFIX.length()));
                } catch (NumberFormatException ignored) {
                    // 非数字角色名，忽略
                }
            }
        }
        return null;
    }

    /**
     * 当前登录人 id（user 表主键）。
     *
     * @return 登录时写入 Authentication.details；改动前建立的旧会话取不到时返回 null
     */
    public static Integer currentUserId() {
        Authentication auth = currentAuthentication();
        if (auth == null) {
            return null;
        }
        Object details = auth.getDetails();
        return details instanceof Integer ? (Integer) details : null;
    }

    /** 当前登录账号（user.no） */
    public static String currentUserNo() {
        Authentication auth = currentAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return null;
        }
        return auth.getPrincipal().toString();
    }
}
