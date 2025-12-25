-- 检查数据库中的任务状态
-- 查看所有接人相关任务

SELECT
    id,
    name,
    destination,
    status,
    created_at,
    created_by
FROM tasks
WHERE name LIKE '%接人%'
ORDER BY created_at DESC;

-- 查看最近的操作日志
SELECT
    operation_type,
    entity_type,
    entity_id,
    description,
    created_at,
    user_id
FROM operation_logs
WHERE entity_type = 'TASK'
    AND (entity_id LIKE '%接人%' OR description LIKE '%接人%')
ORDER BY created_at DESC
LIMIT 10;

-- 查看当前数据库中的所有任务
SELECT COUNT(*) as total_tasks FROM tasks;

-- 查看按创建者分组的任务统计
SELECT
    u.username,
    COUNT(t.id) as task_count,
    GROUP_CONCAT(t.name) as task_names
FROM users u
LEFT JOIN tasks t ON u.id = t.created_by
GROUP BY u.id, u.username;
