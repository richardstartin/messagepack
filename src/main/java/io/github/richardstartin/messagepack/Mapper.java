package io.github.richardstartin.messagepack;

@FunctionalInterface
public interface Mapper<T> {
    void map(T data, Writable packer);
}
