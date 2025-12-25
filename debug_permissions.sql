-- 调试权限控制问题
-- 检查用户、任务和车辆的创建者信息

-- 1. 查看所有用户
SELECT id, username, role FROM users ORDER BY id;

-- 2. 查看所有任务及其创建者
SELECT
    t.id,
    t.name,
    t.created_by,
    u.username as creator_name,
    u.role as creator_role
FROM tasks t
LEFT JOIN users u ON t.created_by = u.id
ORDER BY t.created_at DESC;

-- 3. 查看所有车辆及其创建者
SELECT
    v.id,
    v.driver,
    v.created_by,
    u.username as creator_name,
    u.role as creator_role
FROM vehicles v
LEFT JOIN users u ON v.created_by = u.id
ORDER BY v.created_at DESC;

-- 4. 检查是否有NULL或0值的created_by
SELECT 'Tasks with NULL/0 created_by:' as info;
SELECT COUNT(*) as count FROM tasks WHERE created_by IS NULL OR created_by = 0;

SELECT 'Vehicles with NULL/0 created_by:' as info;
SELECT COUNT(*) as count FROM vehicles WHERE created_by IS NULL OR created_by = 0;
