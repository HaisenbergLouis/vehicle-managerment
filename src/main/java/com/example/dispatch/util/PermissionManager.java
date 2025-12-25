package com.example.dispatch.util;

import com.example.dispatch.model.User;

/**
 * 权限管理类
 * 定义不同角色对系统功能的访问权限
 */
public class PermissionManager {

    // ========== 车辆相关权限 ==========

    /**
     * 检查用户是否有权限查看车辆
     */
    public static boolean canViewVehicles(User user) {
        if (user == null)
            return false;
        String role = user.getRole();
        return UserRoles.ADMIN.equals(role) ||
                UserRoles.DISPATCHER.equals(role) ||
                UserRoles.DRIVER.equals(role);
    }

    /**
     * 检查用户是否有权限创建车辆
     */
    public static boolean canCreateVehicles(User user) {
        if (user == null)
            return false;
        String role = user.getRole();
        return UserRoles.ADMIN.equals(role) ||
                UserRoles.DISPATCHER.equals(role);
    }

    /**
     * 检查用户是否有权限编辑车辆
     */
    public static boolean canEditVehicles(User user) {
        if (user == null)
            return false;
        String role = user.getRole();
        return UserRoles.ADMIN.equals(role) ||
                UserRoles.DISPATCHER.equals(role);
    }

    /**
     * 检查用户是否有权限删除车辆
     */
    public static boolean canDeleteVehicles(User user) {
        if (user == null)
            return false;
        String role = user.getRole();
        return UserRoles.ADMIN.equals(role) ||
                UserRoles.DISPATCHER.equals(role);
    }

    /**
     * 检查用户是否有权限删除特定车辆（自己的车辆或管理员权限）
     */
    public static boolean canDeleteVehicle(User user, int vehicleCreatedBy) {
        if (user == null)
            return false;
        if (UserRoles.ADMIN.equals(user.getRole()))
            return true;
        if (UserRoles.DISPATCHER.equals(user.getRole()))
            return true;
        return user.getId() == vehicleCreatedBy;
    }

    // ========== 任务相关权限 ==========

    /**
     * 检查用户是否有权限查看任务
     */
    public static boolean canViewTasks(User user) {
        if (user == null)
            return false;
        String role = user.getRole();
        return UserRoles.ADMIN.equals(role) ||
                UserRoles.DISPATCHER.equals(role) ||
                UserRoles.DRIVER.equals(role) ||
                UserRoles.CUSTOMER.equals(role);
    }

    /**
     * 检查用户是否有权限创建任务
     */
    public static boolean canCreateTasks(User user) {
        if (user == null)
            return false;
        String role = user.getRole();
        return UserRoles.ADMIN.equals(role) ||
                UserRoles.DISPATCHER.equals(role) ||
                UserRoles.CUSTOMER.equals(role);
    }

    /**
     * 检查用户是否有权限编辑任务
     */
    public static boolean canEditTasks(User user) {
        if (user == null)
            return false;
        String role = user.getRole();
        return UserRoles.ADMIN.equals(role) ||
                UserRoles.DISPATCHER.equals(role);
    }

    /**
     * 检查用户是否有权限分配车辆给任务
     */
    public static boolean canAssignVehicles(User user) {
        if (user == null)
            return false;
        String role = user.getRole();
        return UserRoles.ADMIN.equals(role) ||
                UserRoles.DISPATCHER.equals(role);
    }

    /**
     * 检查用户是否有权限完成任务
     */
    public static boolean canCompleteTasks(User user) {
        if (user == null)
            return false;
        String role = user.getRole();
        return UserRoles.ADMIN.equals(role) ||
                UserRoles.DISPATCHER.equals(role) ||
                UserRoles.DRIVER.equals(role);
    }

    /**
     * 检查用户是否有权限删除任务
     */
    public static boolean canDeleteTasks(User user) {
        if (user == null)
            return false;
        String role = user.getRole();
        return UserRoles.ADMIN.equals(role) ||
                UserRoles.DISPATCHER.equals(role);
    }

    /**
     * 检查用户是否有权限删除特定任务（自己的任务或管理员权限）
     */
    public static boolean canDeleteTask(User user, int taskCreatedBy) {
        if (user == null)
            return false;
        if (UserRoles.ADMIN.equals(user.getRole()))
            return true;
        if (UserRoles.DISPATCHER.equals(user.getRole()))
            return true;
        return user.getId() == taskCreatedBy;
    }

    // ========== 用户管理权限 ==========

    /**
     * 检查用户是否有权限查看用户列表
     */
    public static boolean canViewUsers(User user) {
        if (user == null)
            return false;
        String role = user.getRole();
        return UserRoles.ADMIN.equals(role);
    }

    /**
     * 检查用户是否有权限创建用户
     */
    public static boolean canCreateUsers(User user) {
        if (user == null)
            return false;
        String role = user.getRole();
        return UserRoles.ADMIN.equals(role);
    }

    /**
     * 检查用户是否有权限编辑用户
     */
    public static boolean canEditUsers(User user) {
        if (user == null)
            return false;
        String role = user.getRole();
        return UserRoles.ADMIN.equals(role);
    }

    /**
     * 检查用户是否有权限删除用户
     */
    public static boolean canDeleteUsers(User user) {
        if (user == null)
            return false;
        String role = user.getRole();
        return UserRoles.ADMIN.equals(role);
    }

    // ========== 操作日志权限 ==========

    /**
     * 检查用户是否有权限查看操作日志
     */
    public static boolean canViewOperationLogs(User user) {
        if (user == null)
            return false;
        String role = user.getRole();
        return UserRoles.ADMIN.equals(role) ||
                UserRoles.DISPATCHER.equals(role);
    }

    // ========== 司机特定权限 ==========

    /**
     * 检查用户是否有权限更新车辆状态（司机专用）
     */
    public static boolean canUpdateVehicleStatus(User user) {
        if (user == null)
            return false;
        String role = user.getRole();
        return UserRoles.ADMIN.equals(role) ||
                UserRoles.DISPATCHER.equals(role) ||
                UserRoles.DRIVER.equals(role);
    }

    /**
     * 检查用户是否有权限查看分配给自己的任务
     */
    public static boolean canViewAssignedTasks(User user) {
        if (user == null)
            return false;
        String role = user.getRole();
        return UserRoles.ADMIN.equals(role) ||
                UserRoles.DISPATCHER.equals(role) ||
                UserRoles.DRIVER.equals(role) ||
                UserRoles.CUSTOMER.equals(role);
    }

    // ========== 便捷方法 ==========

    /**
     * 检查用户是否为管理员
     */
    public static boolean isAdmin(User user) {
        return user != null && UserRoles.ADMIN.equals(user.getRole());
    }

    /**
     * 检查用户是否为调度员
     */
    public static boolean isDispatcher(User user) {
        return user != null && UserRoles.DISPATCHER.equals(user.getRole());
    }

    /**
     * 检查用户是否为司机
     */
    public static boolean isDriver(User user) {
        return user != null && UserRoles.DRIVER.equals(user.getRole());
    }

    /**
     * 检查用户是否为客户
     */
    public static boolean isCustomer(User user) {
        return user != null && UserRoles.CUSTOMER.equals(user.getRole());
    }

    /**
     * 获取用户角色显示名称
     */
    public static String getRoleDisplayName(User user) {
        if (user == null)
            return "未登录";
        return UserRoles.getRoleDisplayName(user.getRole());
    }
}
