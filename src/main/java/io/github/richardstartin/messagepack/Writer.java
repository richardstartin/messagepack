package io.github.richardstartin.messagepack;

@FunctionalInterface
public interface Writer<T> {
    void write(T value, Packer writable, EncodingCache encodingCache);
}
