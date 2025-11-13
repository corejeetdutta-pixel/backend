package com.recruitment.util;

import java.security.SecureRandom;

public class ShortIdGenerator {
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int LENGTH = 7; // Bitly-style short code length

    public static String generateShortId() {
        StringBuilder sb = new StringBuilder(LENGTH);

        // Mix in a few random characters
        for (int i = 0; i < LENGTH; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }

        return sb.toString();
    }
}
