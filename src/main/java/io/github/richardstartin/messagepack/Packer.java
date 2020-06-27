package io.github.richardstartin.messagepack;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class Packer {

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

    private static final byte FIXNUM = 0x0;
    private static final int NEGFIXNUM = 0xE0;
    private static final int FIXSTR = 0xA0;
    private static final int FIXARRAY = 0x90;
    private static final int FIXMAP = 0x80;

    private final Codec codec;

    private static final byte[] LZCNT_32 = new byte[] {
            -1,
            INT32,  INT32,  INT32,  INT32,  INT32,  INT32,  INT32, INT32,
            INT32,  INT32,  INT32,  INT32,  INT32,  INT32,  INT32, INT16,
            INT16,  INT16,  INT16,  INT16,  INT16,  INT16,  INT16,  INT8,
            FIXNUM,  FIXNUM, FIXNUM, FIXNUM, FIXNUM, FIXNUM, FIXNUM, FIXNUM
    };

    private static final byte[] LZCNT_32_INV = new byte[] {
            -1,
            INT32,  INT32,  INT32,  INT32,  INT32,  INT32,  INT32, INT32,
            INT32,  INT32,  INT32,  INT32,  INT32,  INT32,  INT32, INT32,
            INT16,  INT16,  INT16,  INT16,  INT16,  INT16,  INT16, INT16,
            INT8,   INT8, FIXNUM, FIXNUM, FIXNUM, FIXNUM, FIXNUM, FIXNUM
    };

    private static final byte[] LZCNT_64 = new byte[] {
            -1,
            INT64, INT64, INT64, INT64, INT64, INT64, INT64, INT64,
            INT64, INT64, INT64, INT64, INT64, INT64, INT64, INT64,
            INT64, INT64, INT64, INT64, INT64, INT64, INT64, INT64,
            INT64, INT64, INT64, INT64, INT64, INT64, INT64, INT32,
            INT32,  INT32,  INT32,  INT32,  INT32,  INT32,  INT32, INT32,
            INT32,  INT32,  INT32,  INT32,  INT32,  INT32,  INT32, INT16,
            INT16,  INT16,  INT16,  INT16,  INT16,  INT16,  INT16,  INT8,
            FIXNUM,  FIXNUM, FIXNUM, FIXNUM, FIXNUM, FIXNUM, FIXNUM, FIXNUM
    };

    private static final byte[] LZCNT_64_INV = new byte[] {
            -1,
            INT64, INT64, INT64, INT64, INT64, INT64, INT64, INT64,
            INT64, INT64, INT64, INT64, INT64, INT64, INT64, INT64,
            INT64, INT64, INT64, INT64, INT64, INT64, INT64, INT64,
            INT64, INT64, INT64, INT64, INT64, INT64, INT64, INT64,
            INT32,  INT32,  INT32,  INT32,  INT32,  INT32,  INT32, INT32,
            INT32,  INT32,  INT32,  INT32,  INT32,  INT32,  INT32, INT32,
            INT16,  INT16,  INT16,  INT16,  INT16,  INT16,  INT16, INT16,
            INT8,   INT8, FIXNUM, FIXNUM, FIXNUM, FIXNUM, FIXNUM, FIXNUM
    };


    private final Consumer<ByteBuffer> blockingSink;

    private final ByteBuffer buffer;

    Packer(Codec codec, Consumer<ByteBuffer> blockingSink, ByteBuffer buffer) {
        this.codec = codec;
        this.blockingSink = blockingSink;
        this.buffer = buffer;
        buffer.mark();
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
        blockingSink.accept(buffer.slice());
        buffer.position(0);
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
                        } else if (i + 1 == s.length()) {
                            buffer.put((byte) '?');
                        } else {
                            char next = s.charAt(i + 1);
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
            } else {
                writeUTF8(utf8, 0, utf8.length);
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
            switch (LZCNT_32_INV[Integer.numberOfLeadingZeros(~value)]) {
                case FIXNUM:
                    buffer.put((byte)(NEGFIXNUM | value));
                    break;
                case INT8:
                    buffer.put(INT8);
                    buffer.put((byte)value);
                    break;
                case INT16:
                    buffer.put(INT16);
                    buffer.putChar((char)value);
                    break;
                case INT32:
                default:
                    buffer.put(INT32);
                    buffer.putInt(value);
            }
        } else {
            switch (LZCNT_32[Integer.numberOfLeadingZeros(value)]) {
                case FIXNUM:
                    buffer.put((byte)value);
                    break;
                case INT8:
                    buffer.put(INT8);
                    buffer.put((byte)value);
                    break;
                case INT16:
                    buffer.put(INT16);
                    buffer.putChar((char)value);
                    break;
                case INT32:
                default:
                    buffer.put(INT32);
                    buffer.putInt(value);
            }
        }
    }

    public void writeLong(long value) {
        if (value < 0) {
            switch (LZCNT_64_INV[Long.numberOfLeadingZeros(~value)]) {
                case FIXNUM:
                    buffer.put((byte)(NEGFIXNUM | value));
                    break;
                case INT8:
                    buffer.put(INT8);
                    buffer.put((byte)value);
                    break;
                case INT16:
                    buffer.put(INT16);
                    buffer.putChar((char)value);
                    break;
                case INT32:
                    buffer.put(INT32);
                    buffer.putInt((int)value);
                case INT64:
                default:
                    buffer.put(INT64);
                    buffer.putLong(value);
            }
        } else {
            switch (LZCNT_64[Long.numberOfLeadingZeros(value)]) {
                case FIXNUM:
                    buffer.put((byte)value);
                    break;
                case INT8:
                    buffer.put(INT8);
                    buffer.put((byte)value);
                    break;
                case INT16:
                    buffer.put(INT16);
                    buffer.putChar((char)value);
                    break;
                case INT32:
                    buffer.put(INT32);
                    buffer.putInt((int)value);
                case INT64:
                default:
                    buffer.put(INT64);
                    buffer.putLong(value);
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
        } else if (length < 0x100) {
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
        }
        else if (length < 0x10000) {
            buffer.put(BIN16);
            buffer.putChar((char)length);
        }
        else {
            buffer.put(BIN32);
            buffer.putInt(length);
        }
    }
}
