package io.github.richardstartin.messagepack;

import java.nio.ByteBuffer;
import java.util.Map;

public interface Writable {
    void writeNull();

    void writeBoolean(boolean value);

    void writeObject(Object value, EncodingCache encodingCache);

    void writeMap(Map<? extends CharSequence, ?> map, EncodingCache encodingCache);

    void writeString(CharSequence s, EncodingCache encodingCache);


    default void writeObject(Object value) {
        writeObject(value, EncodingCachingStrategies.none());
    }

    default void writeMap(Map<? extends CharSequence, ?> map) {
        writeMap(map, EncodingCachingStrategies.none());
    }

    default void writeString(CharSequence s) {
        writeString(s, EncodingCachingStrategies.none());
    }

    void writeUTF8(byte[] string, int offset, int length);

    void writeBinary(byte[] binary, int offset, int length);

    void writeBinary(ByteBuffer buffer);

    void writeInt(int value);

    void writeLong(long value);

    void writeFloat(float value);

    void writeDouble(double value);
}
