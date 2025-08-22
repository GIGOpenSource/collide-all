package com.gig.collide.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具类
 * 提供密码加密和验证功能
 *
 * @author GIG Team
 * @since 1.0.0
 */
@Slf4j
public class PasswordUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 加密密码
     * 
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 验证密码
     * 
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 生成默认密码的加密版本
     * 用于演示目的
     * 
     * @return 默认密码的加密版本
     */
    public static String getDefaultEncodedPassword() {
        return encode("password123");
    }

    /**
     * 验证是否为默认密码
     * 
     * @param rawPassword 原始密码
     * @return 是否为默认密码
     */
    public static boolean isDefaultPassword(String rawPassword) {
        return "password123".equals(rawPassword);
    }
}
