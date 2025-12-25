# 🚗 车辆调度管理系统（JavaFX + MySQL）

一个功能完整的车辆调度管理系统，集成了用户认证、权限控制和智能调度算法。

## ✨ 功能特性

### 🔐 用户认证系统

- **用户注册**：新用户注册账号，选择管理员或普通用户角色
- **用户登录**：安全的用户名密码登录验证
- **密码安全**：SHA-256 加密存储，密码不可逆保护
- **会话管理**：登录状态持久化管理

### 👥 权限控制系统

- **角色管理**：管理员（admin）和普通用户（user）两种角色
- **精细权限**：
  - **管理员权限**：车辆管理、任务分配、系统管理
  - **普通用户权限**：任务创建和管理（不能分配车辆）

### 🚗 车辆调度功能

- **车辆管理**：添加、删除车辆信息（仅管理员）
- **任务管理**：创建、删除任务（所有用户）
- **智能分配**：自动分配车辆给任务（仅管理员）
- **实时状态**：车辆状态实时跟踪和更新

### 💾 数据管理

- **MySQL 集成**：所有数据持久化到 MySQL 数据库
- **自动初始化**：系统首次运行自动创建表结构和默认数据
- **操作日志**：记录所有重要操作和用户活动

## 🚀 快速开始

### 系统要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+

### 1. 准备 MySQL 数据库

确保 MySQL 服务正在运行，并创建数据库：

```sql
CREATE DATABASE vehicle_dispatch CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 配置数据库连接

编辑 `src/main/java/com/example/dispatch/database/DatabaseManager.java`：

```java
private static final String DB_USER = "root"; // 你的MySQL用户名
private static final String DB_PASSWORD = "123456"; // 你的MySQL密码
```

### 3. 运行应用

```bash
mvn clean javafx:run
```

系统会自动创建所需的数据库表和初始数据。

### 4. 默认账号

系统提供默认管理员账号：

- **用户名**：`admin`
- **密码**：`admin123`
- **角色**：管理员

## 📊 权限说明

| 操作功能       | 管理员 (admin) | 普通用户 (user)       |
| -------------- | -------------- | --------------------- |
| **车辆管理**   | ✅ 完全权限    | ❌ 无权限             |
| - 添加车辆     | ✅             | ❌ (按钮禁用)         |
| - 删除车辆     | ✅             | ❌ (按钮禁用)         |
| - 编辑车辆信息 | ✅             | ❌ (输入框禁用)       |
| **任务管理**   | ✅ 完全权限    | ✅ 部分权限           |
| - 新增任务     | ✅             | ✅                    |
| - 删除任务     | ✅             | ✅                    |
| - 分配车辆     | ✅             | ❌ (按钮禁用)         |
| **用户管理**   | ✅             | ❌                    |
| - 用户注册     | ✅             | ✅ (只能注册普通用户) |

## 📁 项目结构

```
src/main/java/com/example/dispatch/
├── MainApp.java              # 应用程序入口
├── database/
│   └── DatabaseManager.java  # 数据库连接管理
├── dao/
│   ├── VehicleDAO.java       # 车辆数据访问层
│   ├── TaskDAO.java          # 任务数据访问层
│   └── UserDAO.java          # 用户数据访问层
├── model/
│   ├── Vehicle.java          # 车辆实体类
│   ├── Task.java             # 任务实体类
│   └── User.java             # 用户实体类
├── service/
│   ├── DispatchService.java  # 调度业务逻辑
│   └── AuthService.java      # 用户认证服务
├── util/
│   ├── DataStore.java        # 数据管理中心
│   ├── SessionManager.java   # 会话管理
│   └── PasswordUtils.java    # 密码加密工具
└── view/
    ├── MainView.java         # 主界面视图
    ├── LoginView.java        # 登录界面
    └── RegisterView.java     # 注册界面
```

## 🛡️ 安全特性

- **密码加密**：使用 SHA-256 哈希算法，密码不可逆存储
- **权限验证**：前端 UI 控制 + 后端权限检查双重保护
- **SQL 注入防护**：使用 PreparedStatement 防止 SQL 注入
- **会话安全**：安全的登录状态管理

## 📈 技术栈

- **前端**：JavaFX (现代化桌面应用框架)
- **后端**：Java 17 (企业级开发语言)
- **数据库**：MySQL 8.0 (关系型数据库)
- **构建工具**：Maven (依赖管理和项目构建)
- **架构模式**：MVC + DAO (清晰的分层架构)

## 📈 代码统计

- **总文件数**：16 个 Java 源文件
- **总代码行数**：约 2000 行
- **架构设计**：MVC 模式 + DAO 模式 + 服务层
- **数据库表**：4 个表（users, vehicles, tasks, operation_logs）

## 🔧 数据库表结构

### 用户表 (users)

```sql
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 车辆表 (vehicles)

```sql
CREATE TABLE vehicles (
    id VARCHAR(20) PRIMARY KEY,
    driver VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT '空闲',
    location VARCHAR(100) NOT NULL DEFAULT '未知',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 任务表 (tasks)

```sql
CREATE TABLE tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    destination VARCHAR(100) NOT NULL,
    eta TIMESTAMP NOT NULL,
    vehicle_id VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT '待分配',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE SET NULL
);
```

## 🎯 使用说明

### 管理员操作

1. 登录管理员账号
2. **车辆管理**：添加新车辆、删除不需要的车辆
3. **任务分配**：为待分配的任务分配合适的车辆
4. **系统监控**：查看所有车辆和任务的状态

### 普通用户操作

1. 注册普通用户账号或使用现有账号登录
2. **任务管理**：创建新的运输任务、删除已完成的任务
3. **状态查看**：查看任务分配情况和车辆状态
4. **权限限制**：无法直接管理车辆或分配任务

## 🤝 贡献

欢迎提交 Issue 和 Pull Request 来改进这个项目！

## 📄 许可证

本项目采用 MIT 许可证。
