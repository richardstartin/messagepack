package io.github.richardstartin.messagepack;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Not thread-safe (use one per thread).
 */
public class Packer {

    private static final int UTF8_BUFFER_SIZE = 1024;
    private static final int MAX_ARRAY_HEADER_SIZE = 5;

    // see https://github.com/msgpack/msgpack/blob/master/spec.md
    private static final byte NULL = (byte)0xC0;

    private static final byte FALSE = (byte)0xC2;
    private static final byte TRUE = (byte)0xC3;

    private static final byte UINT8 = (byte)0xCC;
    private static final byte UINT16 = (byte)0xCD;
    private static final byte UINT32 = (byte)0xCE;
    private static final byte UINT64 = (byte)0xCF;


    private static final byte INT8 = (byte)0xD0;
    private static final byte INT16 = (byte)0xD1;
    private static final byte INT32 = (byte)0xD2;
    private static final byte INT64 = (byte)0xD3;

    private static final byte FLOAT32 = (byte)0xCA;
    private static final byte FLOAT64 = (byte)0xCB;

    private static final byte STR8 = (byte)0xD9;
    private static final byte STR16 = (byte)0xDA;
    private static final byte STR32 = (byte)0xDB;

    private static final byte BIN8 = (byte)0xC4;
    private static final byte BIN16 = (byte)0xC5;
    private static final byte BIN32 = (byte)0xC6;

    private static final byte ARRAY16 = (byte)0xDC;
    private static final byte ARRAY32 = (byte)0xDD;

    private static final byte MAP16 = (byte)0xDE;
    private static final byte MAP32 = (byte)0xDF;

    private static final int NEGFIXNUM = 0xE0;
    private static final int FIXSTR = 0xA0;
    private static final int FIXARRAY = 0x90;
    private static final int FIXMAP = 0x80;

    private final Codec codec;

    private final Consumer<ByteBuffer> blockingSink;
    private final ByteBuffer buffer;
    private int messageCount = 0;

    private static final byte[] UTF8_BUFFER = new byte[UTF8_BUFFER_SIZE];

    public Packer(Codec codec, Consumer<ByteBuffer> blockingSink, ByteBuffer buffer) {
        this.codec = codec;
        this.blockingSink = blockingSink;
        this.buffer = buffer;
        this.buffer.position(MAX_ARRAY_HEADER_SIZE);
        buffer.mark();
    }

    public Packer(Consumer<ByteBuffer> blockingSink, ByteBuffer buffer) {
        this (Codec.INSTANCE, blockingSink, buffer);
    }

    public Packer(Codec codec, Consumer<ByteBuffer> blockingSink, int bufferSize) {
        this (codec, blockingSink, ByteBuffer.allocate(1 << -Long.numberOfLeadingZeros(bufferSize - 1)));
    }

    public <T> void serialise(T message, Mapper<T> mapper) {
        serialise(message, mapper, true);
    }

    private <T> void serialise(T message, Mapper<T> mapper, boolean retry) {
        try {
            mapper.map(message,this);
            buffer.mark();
            ++messageCount;
        } catch (BufferOverflowException e) {
            if (retry) {
                // go back to the last successfully written message
                buffer.reset();
                flush();
                serialise(message, mapper, false);
            } else {
                throw e;
            }
        }
    }

    public void flush() {
        buffer.flip();
        int pos = 0;
        if (messageCount < 0x10) {
            pos = 4;
        } else if (messageCount < 0x10000) {
            pos = 2;
        }
        buffer.position(pos);
        writeArrayHeader(messageCount);
        buffer.position(pos);
        blockingSink.accept(buffer.slice());
        buffer.position(MAX_ARRAY_HEADER_SIZE);
        this.messageCount = 0;
    }

    public void writeNull() {
        buffer.put(NULL);
    }

    public void writeBoolean(boolean value) {
        buffer.put(value ? TRUE : FALSE);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void writeObject(Object value, Function<CharSequence, byte[]> toBytes) {
        if (null == value) {
            writeNull();
        } else {
            Writer writer = codec.get(value.getClass());
            writer.write(value, this, toBytes);
        }
    }

    public void writeMap(Map<? extends CharSequence, Object> map, Function<CharSequence, byte[]> toBytes) {
        writeMapHeader(map.size());
        for (Map.Entry<? extends CharSequence, Object> entry : map.entrySet()) {
            writeString(entry.getKey(), toBytes);
            writeObject(entry.getValue(), toBytes);
        }
    }

    public void writeString(CharSequence s, Function<CharSequence, byte[]> toBytes) {
        if (null == s) {
            writeNull();
        } else {
            byte[] utf8 = toBytes.apply(s);
            if (null == utf8) {
                if (s.length() * 2 < UTF8_BUFFER_SIZE) {
                    encodeToUTF8ViaArray(s);
                } else {
                    encodeToUTF8Direct(s);
                }
            } else {
                writeUTF8(utf8, 0, utf8.length);
            }
        }
    }

    private void encodeToUTF8ViaArray(CharSequence s) {
        byte[] buffer = UTF8_BUFFER;
        writeStringHeader(s.length());
        int out = 0;
        for (int in = 0; in < s.length(); ++in) {
            char c = s.charAt(in);
            if (c < 0x80) {
                buffer[out++] = ((byte) c);
            } else if (c < 0x800) {
                buffer[out++] = ((byte) (0xC0 | (c >> 6)));
                buffer[out++] = ((byte) (0x80 | (c & 0x3F)));
            } else if (Character.isSurrogate(c)) {
                if (!Character.isHighSurrogate(c)) {
                    buffer[out++] = ((byte) '?');
                } else if (++in == s.length()) {
                    buffer[out++] = ((byte) '?');
                } else {
                    char next = s.charAt(in);
                    if (!Character.isLowSurrogate(next)) {
                        buffer[out++] = ((byte) '?');
                        buffer[out++] = (Character.isHighSurrogate(next)? (byte) '?' : (byte)next);
                    } else {
                        int codePoint = Character.toCodePoint(c, next);
                        buffer[out++] = ((byte) (0xF0 | (codePoint >> 18)));
                        buffer[out++] = ((byte) (0x80 | ((codePoint >> 12) & 0x3F)));
                        buffer[out++] = ((byte) (0x80 | ((codePoint >> 6) & 0x3F)));
                        buffer[out++] = ((byte) (0x80 | (codePoint & 0x3F)));
                    }
                }
            }
        }
        this.buffer.put(buffer, 0, out);
    }

    private void encodeToUTF8Direct(CharSequence s) {
        // warning, incurs a lot of bounds checks overhead!
        writeStringHeader(s.length());
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c < 0x80) {
                buffer.put((byte) c);
            } else if (c < 0x800) {
                buffer.put((byte) (0xC0 | (c >> 6)));
                buffer.put((byte) (0x80 | (c & 0x3F)));
            } else if (Character.isSurrogate(c)) {
                if (!Character.isHighSurrogate(c)) {
                    buffer.put((byte) '?');
                } else if (++i == s.length()) {
                    buffer.put((byte) '?');
                } else {
                    char next = s.charAt(i);
                    if (!Character.isLowSurrogate(next)) {
                        buffer.put((byte) '?');
                        buffer.put(Character.isHighSurrogate(next)? (byte) '?' : (byte)next);
                    } else {
                        int codePoint = Character.toCodePoint(c, next);
                        buffer.put((byte) (0xF0 | (codePoint >> 18)));
                        buffer.put((byte) (0x80 | ((codePoint >> 12) & 0x3F)));
                        buffer.put((byte) (0x80 | ((codePoint >> 6) & 0x3F)));
                        buffer.put((byte) (0x80 | (codePoint & 0x3F)));
                    }
                }
            }
        }
    }

    public void writeUTF8(byte[] string, int offset, int length) {
        writeStringHeader(length);
        buffer.put(string, offset, length);
    }

    public void writeBinary(byte[] binary, int offset, int length) {
        writeBinaryHeader(length);
        buffer.put(binary, offset, length);
    }

    public void writeBinary(ByteBuffer buffer) {
        ByteBuffer slice = buffer.slice();
        writeBinaryHeader(slice.limit() - slice.position());
        buffer.put(slice);
    }

    public void writeInt(int value) {
        if (value < 0) {
            switch (Integer.numberOfLeadingZeros(~value)) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                case 15:
                case 16:
                    buffer.put(INT32);
                    buffer.putInt(value);
                    break;
                case 17:
                case 18:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                    buffer.put(INT16);
                    buffer.putChar((char)value);
                    break;
                case 25:
                case 26:
                    buffer.put(INT8);
                    buffer.put((byte)value);
                    break;
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                case 32:
                default:
                    buffer.put((byte)(NEGFIXNUM | value));
            }
        } else {
            switch (Integer.numberOfLeadingZeros(value)) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                case 15:
                    buffer.put(INT32);
                    buffer.putInt(value);
                    break;
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                    buffer.put(INT16);
                    buffer.putChar((char)value);
                    break;
                case 24:
                    buffer.put(INT8);
                    buffer.put((byte)value);
                    break;
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                case 32:
                default:
                    buffer.put((byte)value);
            }
        }
    }

    public void writeLong(long value) {
        if (value < 0) {
            switch (Long.numberOfLeadingZeros(~value)) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                case 15:
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                case 32:
                    buffer.put(INT64);
                    buffer.putLong(value);
                    break;
                case 33:
                case 34:
                case 35:
                case 36:
                case 37:
                case 38:
                case 39:
                case 40:
                case 41:
                case 42:
                case 43:
                case 44:
                case 45:
                case 46:
                case 47:
                case 48:
                    buffer.put(INT32);
                    buffer.putInt((int)value);
                    break;
                case 49:
                case 50:
                case 51:
                case 52:
                case 53:
                case 54:
                case 55:
                case 56:
                    buffer.put(INT16);
                    buffer.putChar((char)value);
                    break;
                case 57:
                case 58:
                    buffer.put(INT8);
                    buffer.put((byte)value);
                    break;
                case 59:
                case 60:
                case 61:
                case 62:
                case 63:
                case 64:
                default:
                    buffer.put((byte)(NEGFIXNUM | value));
            }
        } else {
            switch (Long.numberOfLeadingZeros(value)) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                case 15:
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                    buffer.put(INT64);
                    buffer.putLong(value);
                    break;
                case 32:
                case 33:
                case 34:
                case 35:
                case 36:
                case 37:
                case 38:
                case 39:
                case 40:
                case 41:
                case 42:
                case 43:
                case 44:
                case 45:
                case 46:
                case 47:
                    buffer.put(INT32);
                    buffer.putInt((int)value);
                    break;
                case 48:
                case 49:
                case 50:
                case 51:
                case 52:
                case 53:
                case 54:
                case 55:
                    buffer.put(INT16);
                    buffer.putChar((char)value);
                    break;
                case 56:
                    buffer.put(INT8);
                    buffer.put((byte)value);
                    break;
                case 57:
                case 59:
                case 60:
                case 61:
                case 62:
                case 63:
                case 64:
                default:
                    buffer.put((byte)value);
            }
        }
    }

    public void writeFloat(float value) {
        buffer.put(FLOAT32);
        buffer.putFloat(value);
    }

    public void writeDouble(double value) {
        buffer.put(FLOAT64);
        buffer.putDouble(value);
    }

    public void writeStringHeader(int length) {
        if (length < 0x10) {
            buffer.put((byte)(FIXSTR | length));
        } else if (length < 0x100) {
            buffer.put(STR8);
            buffer.put((byte)length);
        } else if (length < 0x10000) {
            buffer.put(STR16);
            buffer.putChar((char)length);
        } else {
            buffer.put(STR32);
            buffer.putInt(length);
        }
    }

    public void writeArrayHeader(int length) {
        if (length < 0x10) {
            buffer.put((byte) (FIXARRAY | length));
        } else if (length < 0x10000) {
            buffer.put(ARRAY16);
            buffer.putChar((char) length);
        } else {
            buffer.put(ARRAY32);
            buffer.putInt(length);
        }
    }

    public void writeMapHeader(int length) {
        if (length < 0x10) {
            buffer.put((byte)(FIXMAP | length));
        } else if (length < 0x10000) {
            buffer.put(MAP16);
            buffer.putChar((char) length);
        } else {
            buffer.put(MAP32);
            buffer.putInt(length);
        }
    }

    public void writeBinaryHeader(int length) {
        if (length < 0x100) {
            buffer.put(BIN8);
            buffer.put((byte)length);
        } else if (length < 0x10000) {
            buffer.put(BIN16);
            buffer.putChar((char)length);
        } else {
            buffer.put(BIN32);
            buffer.putInt(length);
        }
    }
}
