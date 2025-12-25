package com.example.dispatch.dao;

import com.example.dispatch.database.DatabaseManager;
import com.example.dispatch.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    /**
     * 根据用户名查找用户
     */
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    /**
     * 根据ID查找用户
     */
    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    /**
     * 获取所有用户
     */
    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    /**
     * 保存用户（新增或更新）
     */
    public void save(User user) throws SQLException {
        if (findById(user.getId()) != null) {
            update(user);
        } else {
            insert(user);
        }
    }

    /**
     * 插入新用户
     */
    public void insert(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole());

            stmt.executeUpdate();

            // 记录操作日志
            logOperation("INSERT", "USER", user.getUsername(), "新增用户: " + user.getUsername());
        }
    }

    /**
     * 更新用户信息
     */
    public void update(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, password_hash = ?, role = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole());
            stmt.setInt(4, user.getId());

            stmt.executeUpdate();

            // 记录操作日志
            logOperation("UPDATE", "USER", user.getUsername(), "更新用户: " + user.getUsername());
        }
    }

    /**
     * 删除用户
     */
    public void delete(String username) throws SQLException {
        String sql = "DELETE FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            stmt.executeUpdate();

            // 记录操作日志
            logOperation("DELETE", "USER", username, "删除用户: " + username);
        }
    }

    /**
     * 根据角色查找用户
     */
    public List<User> findByRole(String role) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        }
        return users;
    }

    /**
     * 检查用户名是否存在
     */
    public boolean existsByUsername(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * 获取用户总数
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("role"),
                rs.getTimestamp("created_at").toLocalDateTime());
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
            // 日志记录失败不影响主要操作
            System.err.println("记录操作日志失败: " + e.getMessage());
        }
    }
}