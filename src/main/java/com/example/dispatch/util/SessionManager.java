package com.example.dispatch.util;

import com.example.dispatch.model.User;

public class SessionManager {
    private static User currentUser = null;

    /**
     * 获取当前登录用户
     *
     * @return 当前用户，null表示未登录
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * 设置当前登录用户
     *
     * @param user 用户对象
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    /**
     * 检查用户是否已登录
     *
     * @return 是否已登录
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * 登出当前用户
     */
    public static void logout() {
        currentUser = null;
    }

    /**
     * 获取当前用户名
     *
     * @return 用户名，未登录返回null
     */
    public static String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }

    /**
     * 获取当前用户角色
     *
     * @return 用户角色，未登录返回null
     */
    public static String getCurrentUserRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    /**
     * 获取当前用户角色显示名称
     *
     * @return 角色显示名称，未登录返回"未登录"
     */
    public static String getCurrentUserRoleDisplayName() {
        return PermissionManager.getRoleDisplayName(currentUser);
    }

    // ========== 便捷权限检查方法 ==========

    /**
     * 检查当前用户是否为管理员
     *
     * @return 是否为管理员
     */
    public static boolean isAdmin() {
        return PermissionManager.isAdmin(currentUser);
    }

    /**
     * 检查当前用户是否为调度员
     *
     * @return 是否为调度员
     */
    public static boolean isDispatcher() {
        return PermissionManager.isDispatcher(currentUser);
    }

    /**
     * 检查当前用户是否为司机
     *
     * @return 是否为司机
     */
    public static boolean isDriver() {
        return PermissionManager.isDriver(currentUser);
    }

    /**
     * 检查当前用户是否为客户
     *
     * @return 是否为客户
     */
    public static boolean isCustomer() {
        return PermissionManager.isCustomer(currentUser);
    }

    // ========== 功能权限检查 ==========

    // 车辆权限
    public static boolean canViewVehicles() {
        return PermissionManager.canViewVehicles(currentUser);
    }

    public static boolean canCreateVehicles() {
        return PermissionManager.canCreateVehicles(currentUser);
    }

    public static boolean canEditVehicles() {
        return PermissionManager.canEditVehicles(currentUser);
    }

    public static boolean canDeleteVehicles() {
        return PermissionManager.canDeleteVehicles(currentUser);
    }

    public static boolean canDeleteVehicle(int vehicleCreatedBy) {
        return PermissionManager.canDeleteVehicle(currentUser, vehicleCreatedBy);
    }

    // 任务权限
    public static boolean canViewTasks() {
        return PermissionManager.canViewTasks(currentUser);
    }

    public static boolean canCreateTasks() {
        return PermissionManager.canCreateTasks(currentUser);
    }

    public static boolean canEditTasks() {
        return PermissionManager.canEditTasks(currentUser);
    }

    public static boolean canAssignVehicles() {
        return PermissionManager.canAssignVehicles(currentUser);
    }

    public static boolean canCompleteTasks() {
        return PermissionManager.canCompleteTasks(currentUser);
    }

    public static boolean canDeleteTasks() {
        return PermissionManager.canDeleteTasks(currentUser);
    }

    public static boolean canDeleteTask(int taskCreatedBy) {
        return PermissionManager.canDeleteTask(currentUser, taskCreatedBy);
    }

    // 用户管理权限
    public static boolean canViewUsers() {
        return PermissionManager.canViewUsers(currentUser);
    }

    public static boolean canCreateUsers() {
        return PermissionManager.canCreateUsers(currentUser);
    }

    public static boolean canEditUsers() {
        return PermissionManager.canEditUsers(currentUser);
    }

    public static boolean canDeleteUsers() {
        return PermissionManager.canDeleteUsers(currentUser);
    }

    // 操作日志权限
    public static boolean canViewOperationLogs() {
        return PermissionManager.canViewOperationLogs(currentUser);
    }

    // 司机特定权限
    public static boolean canUpdateVehicleStatus() {
        return PermissionManager.canUpdateVehicleStatus(currentUser);
    }

    public static boolean canViewAssignedTasks() {
        return PermissionManager.canViewAssignedTasks(currentUser);
    }

    // ========== 向后兼容的方法 ==========

    /**
     * 检查当前用户是否有权限访问管理功能
     * （向后兼容，建议使用具体的权限检查方法）
     *
     * @return 是否有权限
     */
    public static boolean hasAdminAccess() {
        return isLoggedIn() && isAdmin();
    }

    /**
     * 检查当前用户是否有调度权限
     * （向后兼容，建议使用具体的权限检查方法）
     *
     * @return 是否有权限
     */
    public static boolean hasDispatchAccess() {
        return isLoggedIn() && (isAdmin() || isDispatcher());
    }
}