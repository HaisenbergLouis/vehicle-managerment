package com.example.dispatch.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    // MySQL连接配置 - 请根据你的环境修改
    private static final String DB_URL = "jdbc:mysql://localhost:3306/vehicle_dispatch?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root"; // 请修改为你的MySQL用户名
    private static final String DB_PASSWORD = "123456"; // 如果MySQL需要密码，请在这里设置

    private static Connection connection;

    // 数据库初始化标志
    private static boolean initialized = false;

    /**
     * 获取数据库连接
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // 加载MySQL驱动
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("MySQL数据库连接成功: " + DB_URL);

                // 如果是首次连接，初始化数据库
                if (!initialized) {
                    initializeDatabase();
                    initialized = true;
                }
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL驱动未找到", e);
            }
        }
        return connection;
    }

    /**
     * 初始化数据库表结构
     */
    private static void initializeDatabase() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // 创建车辆表
            String createVehiclesTable = """
                    CREATE TABLE IF NOT EXISTS vehicles (
                        id VARCHAR(20) PRIMARY KEY,
                        driver VARCHAR(50) NOT NULL,
                        status VARCHAR(20) NOT NULL DEFAULT '空闲',
                        location VARCHAR(100) NOT NULL DEFAULT '未知',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                    """;

            // 创建任务表
            String createTasksTable = """
                    CREATE TABLE IF NOT EXISTS tasks (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(100) NOT NULL UNIQUE,
                        destination VARCHAR(100) NOT NULL,
                        eta TIMESTAMP NOT NULL,
                        vehicle_id VARCHAR(20),
                        status VARCHAR(20) NOT NULL DEFAULT '待分配',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE SET NULL
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                    """;

            // 创建用户表（预留）
            String createUsersTable = """
                    CREATE TABLE IF NOT EXISTS users (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        username VARCHAR(50) NOT NULL UNIQUE,
                        password_hash VARCHAR(255) NOT NULL,
                        role VARCHAR(20) NOT NULL DEFAULT 'user',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                    """;

            // 创建操作日志表
            String createLogsTable = """
                    CREATE TABLE IF NOT EXISTS operation_logs (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        operation_type VARCHAR(20) NOT NULL,
                        entity_type VARCHAR(20) NOT NULL,
                        entity_id VARCHAR(50),
                        description TEXT,
                        user_id INT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                    """;

            // 执行建表语句
            stmt.execute(createVehiclesTable);
            stmt.execute(createTasksTable);
            stmt.execute(createUsersTable);
            stmt.execute(createLogsTable);

            // 创建索引（兼容低版本MySQL）
            // 注意：如果索引已存在会抛出异常，但不影响系统运行
            try {
                stmt.execute("CREATE INDEX idx_vehicle_status ON vehicles(status)");
            } catch (SQLException e) {
                if (!e.getMessage().contains("Duplicate key name")) {
                    throw e; // 只有索引已存在的错误才忽略
                }
            }
            try {
                stmt.execute("CREATE INDEX idx_task_status ON tasks(status)");
            } catch (SQLException e) {
                if (!e.getMessage().contains("Duplicate key name")) {
                    throw e;
                }
            }
            try {
                stmt.execute("CREATE INDEX idx_task_vehicle ON tasks(vehicle_id)");
            } catch (SQLException e) {
                if (!e.getMessage().contains("Duplicate key name")) {
                    throw e;
                }
            }
            try {
                stmt.execute("CREATE INDEX idx_log_operation ON operation_logs(operation_type)");
            } catch (SQLException e) {
                if (!e.getMessage().contains("Duplicate key name")) {
                    throw e;
                }
            }
            try {
                stmt.execute("CREATE INDEX idx_log_created ON operation_logs(created_at)");
            } catch (SQLException e) {
                if (!e.getMessage().contains("Duplicate key name")) {
                    throw e;
                }
            }

            // 插入默认数据（如果表为空）
            insertDefaultData(stmt);

            System.out.println("MySQL数据库初始化完成");
        }
    }

    /**
     * 插入默认测试数据
     */
    private static void insertDefaultData(Statement stmt) throws SQLException {
        // 检查车辆表是否为空
        var rs = stmt.executeQuery("SELECT COUNT(*) FROM vehicles");
        if (rs.next() && rs.getInt(1) == 0) {
            // 插入默认车辆
            stmt.execute("INSERT INTO vehicles (id, driver, status, location) VALUES " +
                    "('A001', '张三', '空闲', '车库'), " +
                    "('A002', '李四', '出车', '机场'), " +
                    "('A003', '王五', '维修', '维修厂')");
            System.out.println("已插入默认车辆数据");
        }

        // 检查任务表是否为空
        rs = stmt.executeQuery("SELECT COUNT(*) FROM tasks");
        if (rs.next() && rs.getInt(1) == 0) {
            // 插入默认任务（使用MySQL的DATE_ADD函数）
            stmt.execute("INSERT INTO tasks (name, destination, eta, status) VALUES " +
                    "('送货-1', '市中心', DATE_ADD(NOW(), INTERVAL 2 HOUR), '待分配'), " +
                    "('接人-1', '火车站', DATE_ADD(NOW(), INTERVAL 1 HOUR), '待分配')");
            System.out.println("已插入默认任务数据");
        }
    }

    /**
     * 关闭数据库连接
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("数据库连接已关闭");
            } catch (SQLException e) {
                System.err.println("关闭数据库连接失败: " + e.getMessage());
            }
        }
    }

    /**
     * 测试数据库连接
     */
    public static boolean testConnection() {
        try {
            Connection testConn = getConnection();
            return testConn != null && !testConn.isClosed();
        } catch (SQLException e) {
            System.err.println("数据库连接测试失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 重置数据库（删除所有表并重新创建）
     * 注意：此操作会删除所有数据，请谨慎使用
     */
    public static void resetDatabase() {
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {

            // 删除现有表
            stmt.execute("DROP TABLE IF EXISTS operation_logs");
            stmt.execute("DROP TABLE IF EXISTS tasks");
            stmt.execute("DROP TABLE IF EXISTS users");
            stmt.execute("DROP TABLE IF EXISTS vehicles");

            // 重置初始化标志
            initialized = false;

            // 重新初始化
            initializeDatabase();

            System.out.println("数据库已重置");

        } catch (SQLException e) {
            System.err.println("数据库重置失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
