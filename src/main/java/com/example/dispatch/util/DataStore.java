package com.example.dispatch.util;

import com.example.dispatch.dao.OperationLogDAO;
import com.example.dispatch.dao.TaskDAO;
import com.example.dispatch.dao.UserDAO;
import com.example.dispatch.dao.VehicleDAO;
import com.example.dispatch.database.DatabaseManager;
import com.example.dispatch.model.Task;
import com.example.dispatch.model.User;
import com.example.dispatch.model.Vehicle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.util.List;

public class DataStore {
        // 内存中的ObservableList，用于JavaFX数据绑定
        public static final ObservableList<Vehicle> VEHICLES = FXCollections.observableArrayList();
        public static final ObservableList<Task> TASKS = FXCollections.observableArrayList();
        public static final ObservableList<User> USERS = FXCollections.observableArrayList();

        // DAO实例
        private static final VehicleDAO vehicleDAO = new VehicleDAO();
        private static final TaskDAO taskDAO = new TaskDAO();
        private static final UserDAO userDAO = new UserDAO();
        private static final OperationLogDAO operationLogDAO = new OperationLogDAO();

        // 为了让DispatchService能访问taskDAO，提供公共访问方法
        public static TaskDAO getTaskDAO() {
                return taskDAO;
        }

        // 初始化标志
        private static boolean initialized = false;

        /**
         * 初始化数据存储，从数据库加载数据
         */
        public static synchronized void initialize() {
                if (initialized) {
                        return; // 防止重复初始化
                }

                try {
                        System.out.println("正在初始化数据存储...");

                        // 从数据库加载车辆数据
                        loadVehiclesFromDatabase();

                        // 从数据库加载任务数据
                        loadTasksFromDatabase();

                        // 从数据库加载用户数据
                        loadUsersFromDatabase();

                        initialized = true;
                        System.out.println("数据存储初始化完成");

                } catch (SQLException e) {
                        System.err.println("数据存储初始化失败: " + e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException("数据库初始化失败", e);
                }
        }

        /**
         * 从数据库加载车辆数据到内存
         */
        private static void loadVehiclesFromDatabase() throws SQLException {
                VEHICLES.clear();
                List<Vehicle> vehicles = vehicleDAO.findAll();
                VEHICLES.addAll(vehicles);
                System.out.println("已加载 " + vehicles.size() + " 辆车辆数据");
        }

        /**
         * 从数据库加载任务数据到内存
         */
        private static void loadTasksFromDatabase() throws SQLException {
                TASKS.clear();
                List<Task> tasks = taskDAO.findAll();
                TASKS.addAll(tasks);
                System.out.println("已加载 " + tasks.size() + " 个任务数据");
        }

        /**
         * 从数据库加载用户数据到内存
         */
        private static void loadUsersFromDatabase() throws SQLException {
                USERS.clear();
                List<User> users = userDAO.findAll();
                USERS.addAll(users);
                System.out.println("已加载 " + users.size() + " 个用户数据");
        }

        // ==================== 车辆相关操作 ====================

        /**
         * 添加车辆
         */
        public static void addVehicle(Vehicle vehicle) throws SQLException {
                // 权限检查：检查是否有权限添加车辆
                if (!SessionManager.canCreateVehicles()) {
                        throw new SecurityException("您没有权限添加车辆");
                }
                // 保存到数据库
                vehicleDAO.save(vehicle);
                // 添加到内存列表
                VEHICLES.add(vehicle);
                System.out.println("车辆已添加: " + vehicle.getId());
        }

        /**
         * 更新车辆信息
         */
        public static void updateVehicle(Vehicle vehicle) throws SQLException {
                // 更新数据库
                vehicleDAO.update(vehicle);

                // 更新内存列表中的对应车辆
                for (int i = 0; i < VEHICLES.size(); i++) {
                        if (VEHICLES.get(i).getId().equals(vehicle.getId())) {
                                VEHICLES.set(i, vehicle);
                                break;
                        }
                }
                System.out.println("车辆已更新: " + vehicle.getId());
        }

        /**
         * 删除车辆
         */
        public static void deleteVehicle(String vehicleId) throws SQLException {
                // 先查找车辆获取创建者信息
                Vehicle vehicle = findVehicleById(vehicleId);
                if (vehicle == null) {
                        throw new IllegalArgumentException("车辆不存在: " + vehicleId);
                }

                // 权限检查：检查是否有权限删除此车辆
                if (!SessionManager.canDeleteVehicle(vehicle.getCreatedBy())) {
                        throw new SecurityException("您没有权限删除此车辆");
                }
                // 从数据库删除
                vehicleDAO.delete(vehicleId);

                // 从内存列表删除
                VEHICLES.removeIf(v -> v.getId().equals(vehicleId));
                System.out.println("车辆已删除: " + vehicleId);
        }

        /**
         * 更新车辆状态
         */
        public static void updateVehicleStatus(String vehicleId, String newStatus) throws SQLException {
                Vehicle vehicle = findVehicleById(vehicleId);
                if (vehicle == null) {
                        throw new IllegalArgumentException("车辆不存在: " + vehicleId);
                }

                vehicle.setStatus(newStatus);
                updateVehicle(vehicle);
                System.out.println("车辆状态已更新: " + vehicleId + " -> " + newStatus);
        }

        /**
         * 更新车辆位置
         */
        public static void updateVehicleLocation(String vehicleId, String newLocation) throws SQLException {
                Vehicle vehicle = findVehicleById(vehicleId);
                if (vehicle == null) {
                        throw new IllegalArgumentException("车辆不存在: " + vehicleId);
                }

                vehicle.setLocation(newLocation);
                updateVehicle(vehicle);
                System.out.println("车辆位置已更新: " + vehicleId + " -> " + newLocation);
        }

        /**
         * 根据ID查找车辆
         */
        public static Vehicle findVehicleById(String id) throws SQLException {
                return vehicleDAO.findById(id);
        }

        /**
         * 获取空闲车辆列表
         */
        public static List<Vehicle> getAvailableVehicles() throws SQLException {
                return vehicleDAO.findByStatus("空闲");
        }

        // ==================== 任务相关操作 ====================

        /**
         * 添加任务
         */
        public static void addTask(Task task) throws SQLException {
                // 保存到数据库
                taskDAO.save(task);
                // 添加到内存列表
                TASKS.add(task);
                System.out.println("任务已添加: " + task.getName());
        }

        /**
         * 更新任务信息
         */
        public static void updateTask(Task task) throws SQLException {
                // 更新数据库
                taskDAO.update(task);

                // 更新内存列表中的对应任务
                for (int i = 0; i < TASKS.size(); i++) {
                        if (TASKS.get(i).getName().equals(task.getName())) {
                                TASKS.set(i, task);
                                break;
                        }
                }
                System.out.println("任务已更新: " + task.getName());
        }

        /**
         * 删除任务
         */
        public static void deleteTask(String taskName) throws SQLException {
                // 先查找任务获取创建者信息
                Task task = findTaskByName(taskName);
                if (task == null) {
                        throw new IllegalArgumentException("任务不存在: " + taskName);
                }

                // 权限检查：检查是否有权限删除此任务
                if (!SessionManager.canDeleteTask(task.getCreatedBy())) {
                        throw new SecurityException("您没有权限删除此任务");
                }

                // 从数据库删除
                taskDAO.delete(taskName);

                // 从内存列表删除
                TASKS.removeIf(t -> t.getName().equals(taskName));
                System.out.println("任务已删除: " + taskName);
        }

        /**
         * 分配车辆给任务
         */
        public static void assignVehicleToTask(String taskName, String vehicleId) throws SQLException {
                // 权限检查：检查是否有权限分配车辆
                if (!SessionManager.canAssignVehicles()) {
                        throw new SecurityException("您没有权限分配车辆");
                }
                // 更新数据库
                taskDAO.assignVehicle(taskName, vehicleId);

                // 更新内存列表中的对应任务
                for (Task task : TASKS) {
                        if (task.getName().equals(taskName)) {
                                task.assignVehicle(vehicleId);
                                break;
                        }
                }

                // 更新车辆状态为"出车"
                Vehicle vehicle = findVehicleById(vehicleId);
                if (vehicle != null) {
                        vehicle.setStatus("出车");
                        updateVehicle(vehicle);
                }

                System.out.println("已分配车辆 " + vehicleId + " 给任务 " + taskName);
        }

        /**
         * 完成任务
         */
        public static void completeTask(String taskName) throws SQLException {
                // 更新数据库
                taskDAO.completeTask(taskName);

                // 更新车辆状态为空闲
                Task task = taskDAO.findByName(taskName);
                if (task != null && task.getVehicleId() != null) {
                        Vehicle vehicle = findVehicleById(task.getVehicleId());
                        if (vehicle != null) {
                                vehicle.setStatus("空闲");
                                updateVehicle(vehicle);
                        }
                }

                // 刷新内存数据
                refreshData();
                System.out.println("任务已完成: " + taskName);
        }

        /**
         * 根据名称查找任务
         */
        public static Task findTaskByName(String name) throws SQLException {
                return taskDAO.findByName(name);
        }

        /**
         * 获取待分配任务列表
         */
        public static List<Task> getPendingTasks() throws SQLException {
                return taskDAO.findByStatus("待分配");
        }

        // ==================== 统计信息 ====================

        /**
         * 获取车辆统计信息
         */
        public static java.util.Map<String, Integer> getVehicleStatistics() throws SQLException {
                return vehicleDAO.getStatusCount();
        }

        /**
         * 获取任务统计信息
         */
        public static java.util.Map<String, Integer> getTaskStatistics() throws SQLException {
                return taskDAO.getStatusCount();
        }

        /**
         * 获取超期任务
         */
        public static List<Task> getOverdueTasks() throws SQLException {
                return taskDAO.findOverdueTasks();
        }

        // ==================== 用户相关操作 ====================

        /**
         * 添加用户
         */
        public static void addUser(User user) throws SQLException {
                // 保存到数据库
                userDAO.save(user);
                // 添加到内存列表
                USERS.add(user);
                System.out.println("用户已添加: " + user.getUsername());
        }

        /**
         * 更新用户信息
         */
        public static void updateUser(User user) throws SQLException {
                // 更新数据库
                userDAO.update(user);

                // 更新内存列表中的对应用户
                for (int i = 0; i < USERS.size(); i++) {
                        if (USERS.get(i).getId() == user.getId()) {
                                USERS.set(i, user);
                                break;
                        }
                }
                System.out.println("用户已更新: " + user.getUsername());
        }

        /**
         * 删除用户
         */
        public static void deleteUser(String username) throws SQLException {
                // 从数据库删除
                userDAO.delete(username);

                // 从内存列表删除
                USERS.removeIf(user -> user.getUsername().equals(username));
                System.out.println("用户已删除: " + username);
        }

        /**
         * 根据用户名查找用户
         */
        public static User findUserByUsername(String username) throws SQLException {
                return userDAO.findByUsername(username);
        }

        /**
         * 根据ID查找用户
         */
        public static User findUserById(int id) throws SQLException {
                return userDAO.findById(id);
        }

        /**
         * 检查用户名是否存在
         */
        public static boolean usernameExists(String username) throws SQLException {
                return userDAO.existsByUsername(username);
        }

        /**
         * 根据角色查找用户
         */
        public static List<User> findUsersByRole(String role) throws SQLException {
                return userDAO.findByRole(role);
        }

        /**
         * 获取用户总数
         */
        public static int getUserCount() throws SQLException {
                return userDAO.count();
        }

        // ==================== 工具方法 ====================

        /**
         * 刷新内存数据（从数据库重新加载）
         */
        public static void refreshData() {
                try {
                        loadVehiclesFromDatabase();
                        loadTasksFromDatabase();
                        loadUsersFromDatabase();
                        System.out.println("数据已刷新");
                } catch (SQLException e) {
                        System.err.println("数据刷新失败: " + e.getMessage());
                        e.printStackTrace();
                }
        }

        /**
         * 获取车辆总数
         */
        public static int getVehicleCount() throws SQLException {
                return vehicleDAO.count();
        }

        /**
         * 获取任务总数
         */
        public static int getTaskCount() throws SQLException {
                return taskDAO.count();
        }

        // ==================== 操作日志相关操作 ==================== //

        /**
         * 添加操作日志
         */
        public static void addOperationLog(com.example.dispatch.model.OperationLog log) throws SQLException {
                operationLogDAO.save(log);
                System.out.println("操作日志已记录: " + log.getOperationType() + " - " + log.getEntityType());
        }

        /**
         * 获取所有操作日志
         */
        public static java.util.List<com.example.dispatch.model.OperationLog> getAllOperationLogs()
                        throws SQLException {
                return operationLogDAO.findAll();
        }

        /**
         * 根据时间范围获取操作日志
         */
        public static java.util.List<com.example.dispatch.model.OperationLog> getOperationLogsByTimeRange(
                        java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) throws SQLException {
                return operationLogDAO.findByTimeRange(startTime, endTime);
        }

        /**
         * 根据操作类型获取操作日志
         */
        public static java.util.List<com.example.dispatch.model.OperationLog> getOperationLogsByType(
                        String operationType) throws SQLException {
                return operationLogDAO.findByOperationType(operationType);
        }

        /**
         * 根据实体类型获取操作日志
         */
        public static java.util.List<com.example.dispatch.model.OperationLog> getOperationLogsByEntityType(
                        String entityType) throws SQLException {
                return operationLogDAO.findByEntityType(entityType);
        }

        /**
         * 根据用户ID获取操作日志
         */
        public static java.util.List<com.example.dispatch.model.OperationLog> getOperationLogsByUserId(int userId)
                        throws SQLException {
                return operationLogDAO.findByUserId(userId);
        }

        /**
         * 删除指定时间之前的操作日志
         */
        public static int deleteOperationLogsBefore(java.time.LocalDateTime beforeTime) throws SQLException {
                return operationLogDAO.deleteBefore(beforeTime);
        }

        /**
         * 获取操作日志总数
         */
        public static int getOperationLogCount() throws SQLException {
                return operationLogDAO.count();
        }

        /**
         * 获取操作日志统计信息
         */
        public static java.util.Map<String, Integer> getOperationLogStatistics() throws SQLException {
                return operationLogDAO.getOperationTypeStatistics();
        }

        /**
         * 获取实体类型统计信息
         */
        public static java.util.Map<String, Integer> getEntityTypeStatistics() throws SQLException {
                return operationLogDAO.getEntityTypeStatistics();
        }

        /**
         * 获取最近N天的日志统计
         */
        public static java.util.Map<String, Integer> getRecentDaysLogStatistics(int days) throws SQLException {
                return operationLogDAO.getRecentDaysStatistics(days);
        }

        /**
         * 关闭数据库连接
         */
        public static void shutdown() {
                DatabaseManager.closeConnection();
        }
}
