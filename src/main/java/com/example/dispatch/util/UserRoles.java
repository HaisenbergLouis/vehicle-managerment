package com.example.dispatch.util;

/**
 * 用户角色定义常量类
 */
public class UserRoles {

    // 角色常量
    public static final String ADMIN = "admin";
    public static final String DISPATCHER = "dispatcher";
    public static final String DRIVER = "driver";
    public static final String CUSTOMER = "customer";
    public static final String USER = "user";

    // 角色显示名称
    public static final String ADMIN_DISPLAY = "管理员";
    public static final String DISPATCHER_DISPLAY = "调度员";
    public static final String DRIVER_DISPLAY = "司机";
    public static final String CUSTOMER_DISPLAY = "客户";
    public static final String USER_DISPLAY = "普通用户";

    /**
     * 获取角色显示名称
     */
    public static String getRoleDisplayName(String role) {
        switch (role) {
            case ADMIN:
                return ADMIN_DISPLAY;
            case DISPATCHER:
                return DISPATCHER_DISPLAY;
            case DRIVER:
                return DRIVER_DISPLAY;
            case CUSTOMER:
                return CUSTOMER_DISPLAY;
            case USER:
                return USER_DISPLAY;
            default:
                return "未知角色";
        }
    }

    /**
     * 获取所有角色选项（用于注册表单）
     */
    public static String[] getAllRoles() {
        return new String[] { CUSTOMER, DRIVER, DISPATCHER, ADMIN };
    }

    /**
     * 获取所有角色显示名称（用于UI显示）
     */
    public static String[] getAllRoleDisplayNames() {
        return new String[] {
                CUSTOMER_DISPLAY,
                DRIVER_DISPLAY,
                DISPATCHER_DISPLAY,
                ADMIN_DISPLAY
        };
    }
}
