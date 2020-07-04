package io.github.richardstartin.messagepack;

public interface Serialiser {
    <T> void serialise(T message, Mapper<T> mapper);

    void flush();
}
