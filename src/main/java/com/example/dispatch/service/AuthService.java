package com.example.dispatch.service;

import com.example.dispatch.dao.UserDAO;
import com.example.dispatch.model.User;
import com.example.dispatch.util.PasswordUtils;

import java.sql.SQLException;

public class AuthService {
    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * 用户登录
     * 
     * @param username 用户名
     * @param password 密码
     * @return 登录成功的用户对象，失败返回null
     */
    public User login(String username, String password) throws SQLException {
        // 验证输入
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }

        // 查找用户
        User user = userDAO.findByUsername(username.trim());
        if (user == null) {
            return null; // 用户不存在
        }

        // 验证密码
        if (PasswordUtils.verifyPassword(password, user.getPasswordHash())) {
            return user; // 登录成功
        } else {
            return null; // 密码错误
        }
    }

    /**
     * 用户注册
     * 
     * @param username 用户名
     * @param password 密码
     * @param role     用户角色，默认为"user"
     * @return 注册成功的用户对象
     */
    public User register(String username, String password, String role) throws SQLException {
        // 验证输入
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("密码长度至少为6位");
        }

        // 检查用户名是否已存在
        if (userDAO.existsByUsername(username.trim())) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // 设置默认角色
        if (role == null || role.trim().isEmpty()) {
            role = "user";
        }

        // 哈希密码
        String passwordHash = PasswordUtils.hashPassword(password);

        // 创建用户对象
        User newUser = new User(username.trim(), passwordHash, role.trim());

        // 保存到数据库
        userDAO.insert(newUser);

        return newUser;
    }

    /**
     * 修改密码
     * 
     * @param username    用户名
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改是否成功
     */
    public boolean changePassword(String username, String oldPassword, String newPassword) throws SQLException {
        // 验证输入
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("新密码不能为空");
        }
        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("新密码长度至少为6位");
        }

        // 验证旧密码
        User user = login(username, oldPassword);
        if (user == null) {
            return false; // 旧密码错误或用户不存在
        }

        // 哈希新密码
        String newPasswordHash = PasswordUtils.hashPassword(newPassword);

        // 更新用户密码
        User updatedUser = new User(user.getId(), user.getUsername(), newPasswordHash, user.getRole(),
                user.getCreatedAt());
        userDAO.update(updatedUser);

        return true;
    }

    /**
     * 检查用户名是否存在
     */
    public boolean usernameExists(String username) throws SQLException {
        return userDAO.existsByUsername(username);
    }

    /**
     * 根据用户名获取用户信息（不包含密码）
     */
    public User getUserByUsername(String username) throws SQLException {
        return userDAO.findByUsername(username);
    }
}