package io.github.richardstartin.messagepack;

import java.util.function.Function;

@FunctionalInterface
public interface Writer<T> {
    void write(T value, Packer packer, Function<CharSequence, byte[]> toBytes);
}
