package com.example.dispatch.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtils {

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 对密码进行哈希处理
     * 
     * @param password 明文密码
     * @return 哈希后的密码字符串
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密码哈希算法不可用", e);
        }
    }

    /**
     * 验证密码是否正确
     * 
     * @param password       明文密码
     * @param hashedPassword 哈希后的密码
     * @return 密码是否匹配
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        String hashedInput = hashPassword(password);
        return hashedInput.equals(hashedPassword);
    }

    /**
     * 生成随机盐值（备用方法）
     * 
     * @return 盐值字符串
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 使用盐值进行密码哈希（备用方法）
     * 
     * @param password 明文密码
     * @param salt     盐值
     * @return 哈希后的密码字符串
     */
    public static String hashPasswordWithSalt(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            digest.update(Base64.getDecoder().decode(salt));
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密码哈希算法不可用", e);
        }
    }
}