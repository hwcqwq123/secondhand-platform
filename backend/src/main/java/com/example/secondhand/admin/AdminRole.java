package com.example.secondhand.admin;

import java.util.Set;

public final class AdminRole {

    private AdminRole() {
    }

    public static final String USER = "USER";

    /**
     * ADMIN 保留兼容原来的老管理员账号。
     * SUPER_ADMIN 是系统唯一超级管理员角色。
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

    /**
     * 后台可以分配的角色。
     * 注意：这里故意不包含 SUPER_ADMIN。
     * SUPER_ADMIN 只能由系统初始化创建，不能由任何管理员手动分配。
     */
    public static final Set<String> ASSIGNABLE_ROLES = Set.of(
            USER,
            ADMIN,
            ITEM_ADMIN,
            ORDER_ADMIN,
            USER_ADMIN,
            SYSTEM_ADMIN
    );

    public static boolean isAdminRole(String role) {
        return role != null && ADMIN_ROLES.contains(role);
    }

    public static boolean isSuperAdminLike(String role) {
        return ADMIN.equals(role) || SUPER_ADMIN.equals(role);
    }

    public static boolean isAssignable(String role) {
        return role != null && ALL_ROLES.contains(role);
    }

    public static boolean isAssignableByAdmin(String role) {
        return role != null && ASSIGNABLE_ROLES.contains(role);
    }

    public static String normalize(String role) {
        if (role == null || role.isBlank()) {
            return USER;
        }
        return role.trim().toUpperCase();
    }
}