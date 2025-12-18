package com.example.dispatch.dao;

import com.example.dispatch.database.DatabaseManager;
import com.example.dispatch.model.Vehicle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO {

    /**
     * 获取所有车辆
     */
    public List<Vehicle> findAll() throws SQLException {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT * FROM vehicles ORDER BY id";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Vehicle vehicle = mapResultSetToVehicle(rs);
                vehicles.add(vehicle);
            }
        }
        return vehicles;
    }

    /**
     * 根据ID查找车辆
     */
    public Vehicle findById(String id) throws SQLException {
        String sql = "SELECT * FROM vehicles WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToVehicle(rs);
                }
            }
        }
        return null;
    }

    /**
     * 根据状态查找车辆
     */
    public List<Vehicle> findByStatus(String status) throws SQLException {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT * FROM vehicles WHERE status = ? ORDER BY id";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vehicle vehicle = mapResultSetToVehicle(rs);
                    vehicles.add(vehicle);
                }
            }
        }
        return vehicles;
    }

    /**
     * 保存车辆（新增或更新）
     */
    public void save(Vehicle vehicle) throws SQLException {
        if (findById(vehicle.getId()) != null) {
            update(vehicle);
        } else {
            insert(vehicle);
        }
    }

    /**
     * 插入新车辆
     */
    public void insert(Vehicle vehicle) throws SQLException {
        String sql = "INSERT INTO vehicles (id, driver, status, location) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vehicle.getId());
            stmt.setString(2, vehicle.getDriver());
            stmt.setString(3, vehicle.getStatus());
            stmt.setString(4, vehicle.getLocation());

            stmt.executeUpdate();

            // 记录操作日志
            logOperation("INSERT", "VEHICLE", vehicle.getId(), "新增车辆: " + vehicle.getId());
        }
    }

    /**
     * 更新车辆信息
     */
    public void update(Vehicle vehicle) throws SQLException {
        String sql = "UPDATE vehicles SET driver = ?, status = ?, location = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vehicle.getDriver());
            stmt.setString(2, vehicle.getStatus());
            stmt.setString(3, vehicle.getLocation());
            stmt.setString(4, vehicle.getId());

            stmt.executeUpdate();

            // 记录操作日志
            logOperation("UPDATE", "VEHICLE", vehicle.getId(), "更新车辆: " + vehicle.getId());
        }
    }

    /**
     * 删除车辆
     */
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM vehicles WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            stmt.executeUpdate();

            // 记录操作日志
            logOperation("DELETE", "VEHICLE", id, "删除车辆: " + id);
        }
    }

    /**
     * 批量保存车辆
     */
    public void saveAll(List<Vehicle> vehicles) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (Vehicle vehicle : vehicles) {
                    save(vehicle);
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
     * 获取车辆总数
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM vehicles";

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
     * 获取各状态车辆数量统计
     */
    public java.util.Map<String, Integer> getStatusCount() throws SQLException {
        String sql = "SELECT status, COUNT(*) as count FROM vehicles GROUP BY status";
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
     * ResultSet映射为Vehicle对象
     */
    private Vehicle mapResultSetToVehicle(ResultSet rs) throws SQLException {
        return new Vehicle(
                rs.getString("id"),
                rs.getString("driver"),
                rs.getString("status"),
                rs.getString("location"));
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
