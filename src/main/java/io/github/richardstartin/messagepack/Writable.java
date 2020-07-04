package io.github.richardstartin.messagepack;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.Function;

public interface Writable {
    void writeNull();

    void writeBoolean(boolean value);

    @SuppressWarnings({"rawtypes", "unchecked"})
    void writeObject(Object value, Function<CharSequence, byte[]> toBytes);

    void writeMap(Map<? extends CharSequence, ? extends Object> map, Function<CharSequence, byte[]> toBytes);

    void writeString(CharSequence s, Function<CharSequence, byte[]> toBytes);

    void writeUTF8(byte[] string, int offset, int length);

    void writeBinary(byte[] binary, int offset, int length);

    void writeBinary(ByteBuffer buffer);

    void writeInt(int value);

    void writeLong(long value);

    void writeFloat(float value);

    void writeDouble(double value);
}
