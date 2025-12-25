package com.example.dispatch.model;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 * 记录系统中的所有操作，用于审计和追踪
 */
public class OperationLog {
    private final int id;
    private final String operationType; // 操作类型
    private final String entityType; // 实体类型
    private final String entityId; // 实体ID
    private final String description; // 操作描述
    private final Integer userId; // 操作用户ID
    private final LocalDateTime createdAt; // 创建时间

    // 操作类型常量
    public static class OperationTypes {
        public static final String CREATE = "CREATE";
        public static final String UPDATE = "UPDATE";
        public static final String DELETE = "DELETE";
        public static final String LOGIN = "LOGIN";
        public static final String LOGOUT = "LOGOUT";
        public static final String VIEW = "VIEW";
        public static final String EXPORT = "EXPORT";
        public static final String IMPORT = "IMPORT";
        public static final String BACKUP = "BACKUP";
        public static final String RESTORE = "RESTORE";
    }

    // 实体类型常量
    public static class EntityTypes {
        public static final String USER = "USER";
        public static final String VEHICLE = "VEHICLE";
        public static final String TASK = "TASK";
        public static final String SYSTEM = "SYSTEM";
        public static final String LOG = "LOG";
    }

    // 构造函数 - 用于数据库查询结果
    public OperationLog(int id, String operationType, String entityType, String entityId,
            String description, Integer userId, LocalDateTime createdAt) {
        this.id = id;
        this.operationType = operationType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.description = description;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    // 构造函数 - 用于创建新日志记录
    public OperationLog(String operationType, String entityType, String entityId,
            String description, Integer userId) {
        this(0, operationType, entityType, entityId, description, userId, LocalDateTime.now());
    }

    // 构造函数 - 用于系统操作（无用户ID）
    public OperationLog(String operationType, String entityType, String entityId, String description) {
        this(0, operationType, entityType, entityId, description, null, LocalDateTime.now());
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getOperationType() {
        return operationType;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getDescription() {
        return description;
    }

    public Integer getUserId() {
        return userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "OperationLog{" +
                "id=" + id +
                ", operationType='" + operationType + '\'' +
                ", entityType='" + entityType + '\'' +
                ", entityId='" + entityId + '\'' +
                ", description='" + description + '\'' +
                ", userId=" + userId +
                ", createdAt=" + createdAt +
                '}';
    }

    /**
     * 获取操作类型显示名称
     */
    public String getOperationTypeDisplayName() {
        switch (operationType) {
            case OperationTypes.CREATE:
                return "创建";
            case OperationTypes.UPDATE:
                return "更新";
            case OperationTypes.DELETE:
                return "删除";
            case OperationTypes.LOGIN:
                return "登录";
            case OperationTypes.LOGOUT:
                return "登出";
            case OperationTypes.VIEW:
                return "查看";
            case OperationTypes.EXPORT:
                return "导出";
            case OperationTypes.IMPORT:
                return "导入";
            case OperationTypes.BACKUP:
                return "备份";
            case OperationTypes.RESTORE:
                return "恢复";
            default:
                return operationType;
        }
    }

    /**
     * 获取实体类型显示名称
     */
    public String getEntityTypeDisplayName() {
        switch (entityType) {
            case EntityTypes.USER:
                return "用户";
            case EntityTypes.VEHICLE:
                return "车辆";
            case EntityTypes.TASK:
                return "任务";
            case EntityTypes.SYSTEM:
                return "系统";
            case EntityTypes.LOG:
                return "日志";
            default:
                return entityType;
        }
    }
}
