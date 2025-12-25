-- 更新现有任务的created_by字段
-- 将所有created_by为NULL或0的任务设置为管理员用户ID (假设管理员ID为1)

-- 首先检查管理员用户ID
SELECT id, username FROM users WHERE role = 'admin' LIMIT 1;

-- 更新现有任务，将created_by设置为管理员ID
-- 这里假设管理员ID为1，如果不同请修改下面的值
UPDATE tasks SET created_by = 1 WHERE created_by IS NULL OR created_by = 0;

-- 检查更新结果
SELECT
    id,
    name,
    created_by,
    (SELECT username FROM users WHERE id = tasks.created_by) as creator_name
FROM tasks
ORDER BY created_at DESC;
