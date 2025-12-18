package com.example.dispatch.dao;

import com.example.dispatch.database.DatabaseManager;
import com.example.dispatch.model.Task;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    // MySQL日期时间格式化器（yyyy-MM-dd HH:mm:ss）
    private static final DateTimeFormatter MYSQL_DATETIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取所有任务
     */
    public List<Task> findAll() throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks ORDER BY created_at DESC";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Task task = mapResultSetToTask(rs);
                tasks.add(task);
            }
        }
        return tasks;
    }

    /**
     * 根据名称查找任务
     */
    public Task findByName(String name) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE name = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTask(rs);
                }
            }
        }
        return null;
    }

    /**
     * 根据状态查找任务
     */
    public List<Task> findByStatus(String status) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE status = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Task task = mapResultSetToTask(rs);
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }

    /**
     * 根据车辆ID查找任务
     */
    public List<Task> findByVehicleId(String vehicleId) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE vehicle_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vehicleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Task task = mapResultSetToTask(rs);
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }

    /**
     * 保存任务（新增或更新）
     */
    public void save(Task task) throws SQLException {
        if (findByName(task.getName()) != null) {
            update(task);
        } else {
            insert(task);
        }
    }

    /**
     * 插入新任务
     */
    public void insert(Task task) throws SQLException {
        String sql = "INSERT INTO tasks (name, destination, eta, vehicle_id, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, task.getName());
            stmt.setString(2, task.getDestination());
            stmt.setString(3, task.getEta().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            stmt.setString(4, task.getVehicleId());
            stmt.setString(5, "待分配"); // 新任务默认状态

            stmt.executeUpdate();

            // 记录操作日志
            logOperation("INSERT", "TASK", task.getName(), "新增任务: " + task.getName());
        }
    }

    /**
     * 更新任务信息
     */
    public void update(Task task) throws SQLException {
        String sql = "UPDATE tasks SET destination = ?, eta = ?, vehicle_id = ?, status = ?, updated_at = CURRENT_TIMESTAMP WHERE name = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, task.getDestination());
            stmt.setString(2, task.getEta().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            stmt.setString(3, task.getVehicleId());
            stmt.setString(4, "进行中"); // 更新的任务设为进行中
            stmt.setString(5, task.getName());

            stmt.executeUpdate();

            // 记录操作日志
            logOperation("UPDATE", "TASK", task.getName(), "更新任务: " + task.getName());
        }
    }

    /**
     * 删除任务
     */
    public void delete(String name) throws SQLException {
        String sql = "DELETE FROM tasks WHERE name = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.executeUpdate();

            // 记录操作日志
            logOperation("DELETE", "TASK", name, "删除任务: " + name);
        }
    }

    /**
     * 分配车辆给任务
     */
    public void assignVehicle(String taskName, String vehicleId) throws SQLException {
        String sql = "UPDATE tasks SET vehicle_id = ?, status = '进行中', updated_at = CURRENT_TIMESTAMP WHERE name = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vehicleId);
            stmt.setString(2, taskName);

            stmt.executeUpdate();

            // 记录操作日志
            logOperation("ASSIGN", "TASK", taskName, "分配车辆 " + vehicleId + " 给任务 " + taskName);
        }
    }

    /**
     * 完成任务
     */
    public void completeTask(String taskName) throws SQLException {
        String sql = "UPDATE tasks SET status = '已完成', updated_at = CURRENT_TIMESTAMP WHERE name = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, taskName);
            stmt.executeUpdate();

            // 记录操作日志
            logOperation("COMPLETE", "TASK", taskName, "完成任务: " + taskName);
        }
    }

    /**
     * 批量保存任务
     */
    public void saveAll(List<Task> tasks) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (Task task : tasks) {
                    save(task);
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * 获取任务总数
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * 获取各状态任务数量统计
     */
    public java.util.Map<String, Integer> getStatusCount() throws SQLException {
        String sql = "SELECT status, COUNT(*) as count FROM tasks GROUP BY status";
        java.util.Map<String, Integer> statusCount = new java.util.HashMap<>();

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                statusCount.put(rs.getString("status"), rs.getInt("count"));
            }
        }
        return statusCount;
    }

    /**
     * 获取超期任务
     */
    public List<Task> findOverdueTasks() throws SQLException {
        List<Task> overdueTasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE eta < CURRENT_TIMESTAMP AND status != '已完成' ORDER BY eta";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Task task = mapResultSetToTask(rs);
                overdueTasks.add(task);
            }
        }
        return overdueTasks;
    }

    /**
     * ResultSet映射为Task对象
     */
    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Task task = new Task(
                rs.getString("name"),
                rs.getString("destination"),
                LocalDateTime.parse(rs.getString("eta"), MYSQL_DATETIME_FORMATTER));
        task.assignVehicle(rs.getString("vehicle_id"));
        return task;
    }

    /**
     * 记录操作日志
     */
    private void logOperation(String operationType, String entityType, String entityId, String description) {
        try {
            String sql = "INSERT INTO operation_logs (operation_type, entity_type, entity_id, description) VALUES (?, ?, ?, ?)";

            try (Connection conn = DatabaseManager.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, operationType);
                stmt.setString(2, entityType);
                stmt.setString(3, entityId);
                stmt.setString(4, description);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("记录操作日志失败: " + e.getMessage());
        }
    }
}
