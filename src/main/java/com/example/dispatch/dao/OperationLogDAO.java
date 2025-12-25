package com.example.dispatch.dao;

import com.example.dispatch.database.DatabaseManager;
import com.example.dispatch.model.OperationLog;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作日志数据访问对象
 */
public class OperationLogDAO {

    /**
     * 保存操作日志
     */
    public void save(OperationLog log) throws SQLException {
        String sql = "INSERT INTO operation_logs (operation_type, entity_type, entity_id, description, user_id, created_at) "
                +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, log.getOperationType());
            stmt.setString(2, log.getEntityType());
            stmt.setString(3, log.getEntityId());
            stmt.setString(4, log.getDescription());
            if (log.getUserId() != null) {
                stmt.setInt(5, log.getUserId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            stmt.setTimestamp(6, Timestamp.valueOf(log.getCreatedAt()));

            stmt.executeUpdate();
        }
    }

    /**
     * 根据ID查找操作日志
     */
    public OperationLog findById(int id) throws SQLException {
        String sql = "SELECT * FROM operation_logs WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOperationLog(rs);
                }
            }
        }
        return null;
    }

    /**
     * 获取所有操作日志
     */
    public List<OperationLog> findAll() throws SQLException {
        String sql = "SELECT * FROM operation_logs ORDER BY created_at DESC";
        List<OperationLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                logs.add(mapResultSetToOperationLog(rs));
            }
        }
        return logs;
    }

    /**
     * 根据时间范围查询操作日志
     */
    public List<OperationLog> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime) throws SQLException {
        String sql = "SELECT * FROM operation_logs WHERE created_at BETWEEN ? AND ? ORDER BY created_at DESC";
        List<OperationLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(startTime));
            stmt.setTimestamp(2, Timestamp.valueOf(endTime));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToOperationLog(rs));
                }
            }
        }
        return logs;
    }

    /**
     * 根据操作类型查询
     */
    public List<OperationLog> findByOperationType(String operationType) throws SQLException {
        String sql = "SELECT * FROM operation_logs WHERE operation_type = ? ORDER BY created_at DESC";
        List<OperationLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, operationType);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToOperationLog(rs));
                }
            }
        }
        return logs;
    }

    /**
     * 根据实体类型查询
     */
    public List<OperationLog> findByEntityType(String entityType) throws SQLException {
        String sql = "SELECT * FROM operation_logs WHERE entity_type = ? ORDER BY created_at DESC";
        List<OperationLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entityType);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToOperationLog(rs));
                }
            }
        }
        return logs;
    }

    /**
     * 根据用户ID查询
     */
    public List<OperationLog> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM operation_logs WHERE user_id = ? ORDER BY created_at DESC";
        List<OperationLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToOperationLog(rs));
                }
            }
        }
        return logs;
    }

    /**
     * 删除指定时间之前的日志
     */
    public int deleteBefore(LocalDateTime beforeTime) throws SQLException {
        String sql = "DELETE FROM operation_logs WHERE created_at < ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(beforeTime));
            return stmt.executeUpdate();
        }
    }

    /**
     * 获取操作日志总数
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM operation_logs";

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
     * 获取按操作类型统计的数据
     */
    public Map<String, Integer> getOperationTypeStatistics() throws SQLException {
        String sql = "SELECT operation_type, COUNT(*) as count FROM operation_logs GROUP BY operation_type";
        Map<String, Integer> stats = new HashMap<>();

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                stats.put(rs.getString("operation_type"), rs.getInt("count"));
            }
        }
        return stats;
    }

    /**
     * 获取按实体类型统计的数据
     */
    public Map<String, Integer> getEntityTypeStatistics() throws SQLException {
        String sql = "SELECT entity_type, COUNT(*) as count FROM operation_logs GROUP BY entity_type";
        Map<String, Integer> stats = new HashMap<>();

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                stats.put(rs.getString("entity_type"), rs.getInt("count"));
            }
        }
        return stats;
    }

    /**
     * 获取最近N天的日志统计
     */
    public Map<String, Integer> getRecentDaysStatistics(int days) throws SQLException {
        String sql = "SELECT DATE(created_at) as date, COUNT(*) as count " +
                "FROM operation_logs " +
                "WHERE created_at >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                "GROUP BY DATE(created_at) " +
                "ORDER BY date DESC";
        Map<String, Integer> stats = new HashMap<>();

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, days);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    stats.put(rs.getString("date"), rs.getInt("count"));
                }
            }
        }
        return stats;
    }

    /**
     * 将ResultSet映射为OperationLog对象
     */
    private OperationLog mapResultSetToOperationLog(ResultSet rs) throws SQLException {
        Integer userId = rs.getInt("user_id");
        if (rs.wasNull()) {
            userId = null;
        }

        return new OperationLog(
                rs.getInt("id"),
                rs.getString("operation_type"),
                rs.getString("entity_type"),
                rs.getString("entity_id"),
                rs.getString("description"),
                userId,
                rs.getTimestamp("created_at").toLocalDateTime());
    }
}
