package io.github.richardstartin.messagepack;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public class Codec extends ClassValue<Writer<?>> {

    public static final Codec INSTANCE = new Codec();

    private final Map<Class<?>, Writer<?>> config;

    public Codec(Map<Class<?>, Writer<?>> config) {
        this.config = config;
    }

    public Codec() {
        this(Collections.emptyMap());
    }

    @Override
    protected Writer<?> computeValue(Class<?> clazz) {
        Writer<?> writer = config.get(clazz);
        if (null != writer) {
            return writer;
        }
        if (Number.class.isAssignableFrom(clazz)) {
            if (Double.class == clazz) {
                return new DoubleWriter();
            }
            if (Float.class == clazz) {
                return new FloatWriter();
            }
            if (Integer.class == clazz) {
                return new IntWriter();
            }
            if (Long.class == clazz) {
                return new LongWriter();
            }
            if (Short.class == clazz) {
                return new ShortWriter();
            }
        }
        if (Boolean.class == clazz) {
            return new BooleanWriter();
        }
        if (CharSequence.class.isAssignableFrom(clazz)) {
            return CharSequenceWriter.INSTANCE;
        }
        if (Map.class.isAssignableFrom(clazz)) {
            return new MapWriter();
        }
        if (Collection.class.isAssignableFrom(clazz)) {
            return new CollectionWriter();
        }
        if (clazz.isArray()) {
            if (byte[].class == clazz) {
                return new ByteArrayWriter();
            }
            if (int[].class == clazz) {
                return new IntArrayWriter();
            }
            if (long[].class == clazz) {
                return new LongArrayWriter();
            }
            if (double[].class == clazz) {
                return new DoubleArrayWriter();
            }
            if (float[].class == clazz) {
                return new FloatArrayWriter();
            }
            if (short[].class == clazz) {
                return new ShortArrayWriter();
            }
            if (char[].class == clazz) {
                return new CharArrayWriter();
            }
            if (boolean[].class == clazz) {
                return new BooleanArrayWriter();
            }
            return new ObjectArrayWriter();
        }
        return DefaultWriter.INSTANCE;
    }

    private static final class IntArrayWriter implements Writer<int[]> {

        @Override
        public void write(int[] value, Packer packer, Function<CharSequence, byte[]> toBytes) {
            packer.writeArrayHeader(value.length);
            for (int i : value) {
                packer.writeInt(i);
            }
        }
    }

    private static final class ShortArrayWriter implements Writer<short[]> {

        @Override
        public void write(short[] value, Packer packer, Function<CharSequence, byte[]> toBytes) {
            packer.writeArrayHeader(value.length);
            for (short i : value) {
                packer.writeInt(i);
            }
        }
    }

    private static final class ByteArrayWriter implements Writer<byte[]> {

        @Override
        public void write(byte[] value, Packer packer, Function<CharSequence, byte[]> toBytes) {
            packer.writeBinary(value, 0, value.length);
        }
    }

    private static final class ByteBufferWriter implements Writer<ByteBuffer> {

        @Override
        public void write(ByteBuffer buffer, Packer packer, Function<CharSequence, byte[]> toBytes) {
            packer.writeBinary(buffer);
        }
    }

    private static final class BooleanArrayWriter implements Writer<boolean[]> {

        @Override
        public void write(boolean[] value, Packer packer, Function<CharSequence, byte[]> toBytes) {
            packer.writeArrayHeader(value.length);
            for (boolean i : value) {
                packer.writeBoolean(i);
            }
        }
    }

    private static final class DoubleArrayWriter implements Writer<double[]> {

        @Override
        public void write(double[] value, Packer packer, Function<CharSequence, byte[]> toBytes) {
            packer.writeArrayHeader(value.length);
            for (double i : value) {
                packer.writeDouble(i);
            }
        }
    }


    private static final class FloatArrayWriter implements Writer<float[]> {

        @Override
        public void write(float[] value, Packer packer, Function<CharSequence, byte[]> toBytes) {
            packer.writeArrayHeader(value.length);
            for (float i : value) {
                packer.writeFloat(i);
            }
        }
    }


    private static final class LongArrayWriter implements Writer<long[]> {

        @Override
        public void write(long[] value, Packer packer, Function<CharSequence, byte[]> toBytes) {
            packer.writeArrayHeader(value.length);
            for (long i : value) {
                packer.writeLong(i);
            }
        }
    }

    private static class CollectionWriter implements Writer<Collection<?>> {

        @Override
        public void write(Collection<?> collection, Packer packer, Function<CharSequence, byte[]> toBytes) {
            packer.writeArrayHeader(collection.size());
            for (Object value : collection) {
                packer.writeObject(value, toBytes);
            }
        }
    }

    private static class ObjectArrayWriter implements Writer<Object[]> {

        @Override
        public void write(Object[] array, Packer packer, Function<CharSequence, byte[]> toBytes) {
            packer.writeArrayHeader(array.length);
            for (Object value : array) {
                packer.writeObject(value, toBytes);
            }
        }
    }

    private static class MapWriter implements Writer<Map<? extends CharSequence, Object>> {

        @Override
        public void write(Map<? extends CharSequence, Object> value, Packer packer, Function<CharSequence, byte[]> toBytes) {
            packer.writeMap(value, toBytes);
        }
    }

    private static class DoubleWriter implements Writer<Double> {

        @Override
        public void write(Double value, Packer packer, Function<CharSequence, byte[]> toBytes) {
            packer.writeDouble(value);
        }
    }

    private static class BooleanWriter implements Writer<Boolean> {

        @Override
        public void write(Boolean value, Packer packer, Function<CharSequence, byte[]> toBytes) {
            packer.writeBoolean(value);
        }
    }

    private static class FloatWriter implements Writer<Float> {

        @Override
        public void write(Float value, Packer packer, Function<CharSequence, byte[]> toBytes) {
            packer.writeFloat(value);
        }
    }

    private static class IntWriter implements Writer<Integer> {

        @Override
        public void write(Integer value, Packer packer, Function<CharSequence, byte[]> toBytes) {
            packer.writeInt(value);
        }
    }

    private static class ShortWriter implements Writer<Short> {

        @Override
        public void write(Short value, Packer packer, Function<CharSequence, byte[]> toBytes) {
            packer.writeInt(value);
        }
    }

    private static class LongWriter implements Writer<Long> {

        @Override
        public void write(Long value, Packer packer, Function<CharSequence, byte[]> toBytes) {
            packer.writeLong(value);
        }
    }

    private static class CharSequenceWriter implements Writer<CharSequence> {

        public static final CharSequenceWriter INSTANCE = new CharSequenceWriter();

        @Override
        public void write(CharSequence value, Packer packer, Function<CharSequence, byte[]> toBytes) {
            byte[] bytes = toBytes.apply(value);
            if (null == bytes) {
                packer.writeStringHeader(value.length());
                ByteBuffer buffer = packer.shareBuffer();
                for (int i = 0; i < value.length(); ++i) {
                    char c = value.charAt(i);
                    if (c < 0x80) {
                        buffer.put((byte) c);
                    } else if (c < 0x800) {
                        buffer.put((byte) (0xC0 | (c >> 6)));
                        buffer.put((byte) (0x80 | (c & 0x3F)));
                    } else if (Character.isSurrogate(c)) {
                        if (!Character.isHighSurrogate(c)) {
                            buffer.put((byte) '?');
                        } else if (++i == value.length()) {
                            buffer.put((byte) '?');
                        } else {
                            char next = value.charAt(i);
                            if (!Character.isLowSurrogate(next)) {
                                buffer.put((byte) '?');
                                buffer.put(Character.isHighSurrogate(next)? (byte) '?' : (byte)next);
                            } else {
                                int codePoint = Character.toCodePoint(c, next);
                                buffer.put((byte) (0xf0 | (codePoint >> 18)));
                                buffer.put((byte) (0x80 | ((codePoint >> 12) & 0x3F)));
                                buffer.put((byte) (0x80 | ((codePoint >> 6) & 0x3F)));
                                buffer.put((byte) (0x80 | (codePoint & 0x3F)));
                            }
                        }
                    }
                }
            } else {
                packer.writeUTF8(bytes, 0, bytes.length);
            }
        }
    }

    private static class CharArrayWriter implements Writer<char[]> {

        public static final CharSequenceWriter INSTANCE = new CharSequenceWriter();

        @Override
        public void write(char[] value, Packer packer, Function<CharSequence, byte[]> toBytes) {
            packer.writeStringHeader(value.length);
            ByteBuffer buffer = packer.shareBuffer();
            for (int i = 0; i < value.length; ++i) {
                char c = value[i];
                if (c < 0x80) {
                    buffer.put((byte) c);
                } else if (c < 0x800) {
                    buffer.put((byte) (0xC0 | (c >> 6)));
                    buffer.put((byte) (0x80 | (c & 0x3F)));
                } else if (Character.isSurrogate(c)) {
                    if (!Character.isHighSurrogate(c)) {
                        buffer.put((byte) '?');
                    } else if (++i == value.length) {
                        buffer.put((byte) '?');
                    } else {
                        char next = value[i];
                        if (!Character.isLowSurrogate(next)) {
                            buffer.put((byte) '?');
                            buffer.put(Character.isHighSurrogate(next) ? (byte) '?' : (byte) next);
                        } else {
                            int codePoint = Character.toCodePoint(c, next);
                            buffer.put((byte) (0xf0 | (codePoint >> 18)));
                            buffer.put((byte) (0x80 | ((codePoint >> 12) & 0x3F)));
                            buffer.put((byte) (0x80 | ((codePoint >> 6) & 0x3F)));
                            buffer.put((byte) (0x80 | (codePoint & 0x3F)));
                        }
                    }
                }
            }
        }
    }

    private static class DefaultWriter implements Writer<Object> {

        public static final DefaultWriter INSTANCE = new DefaultWriter();

        @Override
        public void write(Object value, Packer packer, Function<CharSequence, byte[]> toBytes) {
            CharSequenceWriter.INSTANCE.write(String.valueOf(value), packer, NoOp.INSTANCE);
        }

        private static class NoOp implements Function<CharSequence, byte[]> {

            public static final NoOp INSTANCE = new NoOp();

            @Override
            public byte[] apply(CharSequence s) {
                return null;
            }
        }
    }
}
