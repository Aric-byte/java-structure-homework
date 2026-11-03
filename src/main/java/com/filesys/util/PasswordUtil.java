package com.filesys.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * 密码工具：SHA-256 加盐。
 *   存储值 = hex(SHA-256(salt + 明文))，与 sql/filesys.sql 中 SHA2(CONCAT(salt, '123456'), 256) 一致。
 * 每个用户一个随机盐，相同密码的哈希也不同，防彩虹表。
 */
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String newSalt() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    public static String hash(String salt, String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((salt + rawPassword).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("JDK 不支持 SHA-256", e);
        }
    }

    /** 常量时间比较，避免按比较耗时推测哈希 */
    public static boolean matches(String rawPassword, String salt, String storedHash) {
        if (rawPassword == null || salt == null || storedHash == null) {
            return false;
        }
        return MessageDigest.isEqual(
                hash(salt, rawPassword).getBytes(StandardCharsets.US_ASCII),
                storedHash.toLowerCase().getBytes(StandardCharsets.US_ASCII));
    }
}
