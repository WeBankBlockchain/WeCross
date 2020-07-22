package com.webank.wecross.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.bouncycastle.util.encoders.Hex;

public class HashUtils {
    /**
     * Generates SHA-256 digest for the given {@code input}.
     *
     * @param input The input to digest
     * @return hash value as hex encoded string
     * @throws RuntimeException If we couldn't find any SHA-256 provider
     */
    public static String sha256String(byte[] input) {
        byte[] res = sha256(input);
        StringBuilder stringBuilder = new StringBuilder(2 + res.length * 2);
        stringBuilder.append("0x").append(Hex.toHexString(res));
        return stringBuilder.toString();
    }

    /**
     * Generates SHA-256 digest for the given {@code input}.
     *
     * @param input The input to digest
     * @return The hash value for the given input
     * @throws RuntimeException If we couldn't find any SHA-256 provider
     */
    public static byte[] sha256(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Couldn't find a SHA-256 provider", e);
        }
    }
}
