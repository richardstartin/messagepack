package io.github.richardstartin.messagepack;

@FunctionalInterface
public interface EncodingCache {

    byte[] encode(CharSequence s);

}
