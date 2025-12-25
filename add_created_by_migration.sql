-- 数据库迁移脚本：添加created_by字段
-- 用于支持用户只能删除自己添加的信息的功能

-- 为vehicles表添加created_by字段
ALTER TABLE vehicles ADD COLUMN created_by INT;
ALTER TABLE vehicles ADD CONSTRAINT fk_vehicles_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL;

-- 为tasks表添加created_by字段
ALTER TABLE tasks ADD COLUMN created_by INT;
ALTER TABLE tasks ADD CONSTRAINT fk_tasks_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL;

-- 为现有数据设置默认创建者（管理员用户ID为1）
UPDATE vehicles SET created_by = 1 WHERE created_by IS NULL;
UPDATE tasks SET created_by = 1 WHERE created_by IS NULL;

-- 创建索引以提高查询性能
CREATE INDEX idx_vehicles_created_by ON vehicles(created_by);
CREATE INDEX idx_tasks_created_by ON tasks(created_by);
