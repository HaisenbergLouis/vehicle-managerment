-- 更新现有车辆的created_by字段
-- 将所有created_by为NULL或0的车辆设置为管理员用户ID

-- 更新现有车辆，将created_by设置为管理员ID
UPDATE vehicles SET created_by = 1 WHERE created_by IS NULL OR created_by = 0;

-- 检查更新结果
SELECT
    id,
    driver,
    created_by,
    (SELECT username FROM users WHERE id = vehicles.created_by) as creator_name
FROM vehicles
ORDER BY created_at DESC;
