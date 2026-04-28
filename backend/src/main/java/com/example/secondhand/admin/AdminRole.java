package com.example.secondhand.admin;

import java.util.Set;

public final class AdminRole {

    private AdminRole() {
    }

    public static final String USER = "USER";

    /**
     * ADMIN 保留兼容原来的老管理员账号。
     * SUPER_ADMIN 是唯一超级管理员角色，只允许固定账号 admin 拥有。
     */
    public static final String ADMIN = "ADMIN";
    public static final String SUPER_ADMIN = "SUPER_ADMIN";

    public static final String ITEM_ADMIN = "ITEM_ADMIN";
    public static final String ORDER_ADMIN = "ORDER_ADMIN";
    public static final String USER_ADMIN = "USER_ADMIN";
    public static final String SYSTEM_ADMIN = "SYSTEM_ADMIN";

    public static final Set<String> ALL_ROLES = Set.of(
            USER,
            ADMIN,
            SUPER_ADMIN,
            ITEM_ADMIN,
            ORDER_ADMIN,
            USER_ADMIN,
            SYSTEM_ADMIN
    );

    public static final Set<String> ADMIN_ROLES = Set.of(
            ADMIN,
            SUPER_ADMIN,
            ITEM_ADMIN,
            ORDER_ADMIN,
            USER_ADMIN,
            SYSTEM_ADMIN
    );

    public static boolean isAdminRole(String role) {
        return role != null && ADMIN_ROLES.contains(role);
    }

    public static boolean isSuperAdminLike(String role) {
        return SUPER_ADMIN.equals(role);
    }

    public static boolean isAssignable(String role) {
        return role != null && ALL_ROLES.contains(role);
    }

    public static String normalize(String role) {
        if (role == null || role.isBlank()) {
            return USER;
        }
        return role.trim().toUpperCase();
    }
}