package io.github.richardstartin.messagepack;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

public class Strings {

    private static final byte[] LOWER_CASE = new byte[26];
    static {
        for (int i = 'a'; i <= 'z'; ++i) {
            LOWER_CASE[i - 'a'] = (byte)i;
        }
    }

    public static String create(int size) {
        byte[] bytes = new byte[size];
        for (int i = 0; i < size; ++i) {
            bytes[i] = LOWER_CASE[ThreadLocalRandom.current().nextInt(26)];
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
