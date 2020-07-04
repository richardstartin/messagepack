package io.github.richardstartin.messagepack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class EncodingCachingStrategies {

    private static final EncodingCache NONE = s -> null;

    public static EncodingCache none() {
        return NONE;
    }

    public static EncodingCache constantPool(Map<? extends CharSequence, byte[]> constants) {
        return constants::get;
    }

    public static EncodingCache memoise(Function<CharSequence, byte[]> fn) {
        Map<CharSequence, byte[]> map = new HashMap<>();
        return s -> map.computeIfAbsent(s, fn);
    }
}
