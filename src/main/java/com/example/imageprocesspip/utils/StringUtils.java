package com.example.imageprocesspip.utils;

import java.security.SecureRandom;

public class StringUtils {

    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();

    public static String generateRandomAlphanumeric(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("String length must be a positive integer");
        }

        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(ALPHANUMERIC_CHARS.length());
            sb.append(ALPHANUMERIC_CHARS.charAt(randomIndex));
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        String randomAlphanumeric = generateRandomAlphanumeric(8);
        System.out.println(randomAlphanumeric);
    }
}
