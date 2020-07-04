package io.github.richardstartin.messagepack;

import org.junit.Assert;
import org.junit.Test;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class PackerTest {

    @Test(expected = BufferOverflowException.class)
    public void testOverflow() {
        Serialiser packer = new Packer(buffer -> {}, ByteBuffer.allocate(25));
        packer.serialise(new HashMap<>() {{
            put("foo", "abcdefghijklmnopqrstuvwxyz");
        }}, (Mapper<HashMap<? extends Object, ? extends Object>>) (data, writable) -> writable.writeObject(data, x -> null));
    }

    @Test
    public void testRecycle() {
        AtomicInteger i = new AtomicInteger();
        Map<String, String> map = new HashMap<>() {{
            put("foo", "abcd");
        }};
        Packer packer = new Packer(buffer -> i.getAndIncrement(), ByteBuffer.allocate(25));
        Mapper mapper = (data, packer1) -> packer1.writeObject(data, x -> null);
        packer.serialise(map, mapper);
        packer.serialise(map, mapper);
        packer.serialise(map, mapper);
        packer.serialise(map, mapper);
        assertEquals(1, i.getAndIncrement());
    }

    @Test
    public void testWriteBinary() {
        final byte[] data = new byte[] { 1, 2, 3, 4};
        Serialiser serialiser = new Packer(buffy -> {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(buffy);
            try {
                assertEquals(1, unpacker.unpackArrayHeader());
                int length = unpacker.unpackBinaryHeader();
                assertEquals(4, length);
                assertArrayEquals(data, unpacker.readPayload(length));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }, ByteBuffer.allocate(25));
        serialiser.serialise(data, (ba, writable) -> writable.writeBinary(ba, 0, ba.length));
        serialiser.flush();
    }

    @Test
    public void testWriteBinaryAsObject() {
        final byte[] data = new byte[] { 1, 2, 3, 4};
        Serialiser serialiser = new Packer(buffy -> {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(buffy);
            try {
                assertEquals(1, unpacker.unpackArrayHeader());
                int length = unpacker.unpackBinaryHeader();
                assertEquals(4, length);
                assertArrayEquals(data, unpacker.readPayload(length));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }, ByteBuffer.allocate(25));
        serialiser.serialise(data, (ba, writable) -> writable.writeObject(ba, s -> null));
        serialiser.flush();
    }

    @Test
    public void testWriteByteBuffer() {
        final byte[] data = new byte[] { 1, 2, 3, 4};
        Serialiser serialiser = new Packer(buffy -> {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(buffy);
            try {
                assertEquals(1, unpacker.unpackArrayHeader());
                int length = unpacker.unpackBinaryHeader();
                assertEquals(4, length);
                assertArrayEquals(data, unpacker.readPayload(length));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }, ByteBuffer.allocate(25));
        serialiser.serialise(ByteBuffer.wrap(data), (bb, writable) -> writable.writeBinary(bb));
        serialiser.flush();
    }

    @Test
    public void testWriteByteBufferAsObject() {
        final byte[] data = new byte[] { 1, 2, 3, 4};
        Serialiser serialiser = new Packer(buffy -> {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(buffy);
            try {
                assertEquals(1, unpacker.unpackArrayHeader());
                int length = unpacker.unpackBinaryHeader();
                assertEquals(4, length);
                assertArrayEquals(data, unpacker.readPayload(length));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }, ByteBuffer.allocate(25));
        serialiser.serialise(ByteBuffer.wrap(data), (bb, writable) -> writable.writeObject(bb, s -> null));
        serialiser.flush();
    }

    @Test
    public void testWriteNull() {
        Serialiser serialiser = new Packer(buffy -> {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(buffy);
            try {
                assertEquals(1, unpacker.unpackArrayHeader());
                unpacker.unpackNil();
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }, ByteBuffer.allocate(25));
        serialiser.serialise(null, (x, w) -> w.writeObject(x, s -> null));
        serialiser.flush();
    }

    @Test
    public void testWriteBooleanAsObject() {
        Serialiser serialiser = new Packer(buffy -> {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(buffy);
            try {
                assertEquals(1, unpacker.unpackArrayHeader());
                assertTrue(unpacker.unpackBoolean());
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }, ByteBuffer.allocate(25));
        serialiser.serialise(true, (x, w) -> w.writeObject(x, s -> null));
        serialiser.flush();
    }

    @Test
    public void testWriteBoolean() {
        Serialiser serialiser = new Packer(buffy -> {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(buffy);
            try {
                assertEquals(1, unpacker.unpackArrayHeader());
                assertTrue(unpacker.unpackBoolean());
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }, ByteBuffer.allocate(25));
        serialiser.serialise(true, (x, w) -> w.writeBoolean(x));
        serialiser.flush();
    }


    @Test
    public void testWriteCharArray() {
        final String data = "xyz";
        Serialiser serialiser = new Packer(buffy -> {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(buffy);
            try {
                assertEquals(1, unpacker.unpackArrayHeader());
                assertEquals(data, unpacker.unpackString());
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }, ByteBuffer.allocate(25));
        serialiser.serialise(data.toCharArray(), (x, w) -> w.writeObject(x, s -> null));
        serialiser.flush();
    }


    @Test
    public void testWriteBooleanArray() {
        final boolean[] data = new boolean[] { true, false, true, true};
        Serialiser serialiser = new Packer(buffy -> {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(buffy);
            try {
                assertEquals(1, unpacker.unpackArrayHeader());
                assertEquals(4, unpacker.unpackArrayHeader());
                for (boolean datum : data) {
                    assertEquals(datum, unpacker.unpackBoolean());
                }
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }, ByteBuffer.allocate(25));
        serialiser.serialise(data, (x, w) -> w.writeObject(x, s -> null));
        serialiser.flush();
    }

    @Test
    public void testWriteFloatArray() {
        final float[] data = new float[] { 0.1f, 0.2f, 0.3f, 0.4f};
        Serialiser serialiser = new Packer(buffy -> {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(buffy);
            try {
                assertEquals(1, unpacker.unpackArrayHeader());
                assertEquals(4, unpacker.unpackArrayHeader());
                for (float datum : data) {
                    assertEquals(datum, unpacker.unpackFloat(), 0.001);
                }
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }, ByteBuffer.allocate(100));
        serialiser.serialise(data, (x, w) -> w.writeObject(x, s -> null));
        serialiser.flush();
    }

    @Test
    public void testWriteDoubleArray() {
        final double[] data = new double[] { 0.1f, 0.2f, 0.3f, 0.4f};
        Serialiser serialiser = new Packer(buffy -> {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(buffy);
            try {
                assertEquals(1, unpacker.unpackArrayHeader());
                assertEquals(4, unpacker.unpackArrayHeader());
                for (double datum : data) {
                    assertEquals(datum, unpacker.unpackDouble(), 0.001);
                }
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }, ByteBuffer.allocate(100));
        serialiser.serialise(data, (x, w) -> w.writeObject(x, s -> null));
        serialiser.flush();
    }

    @Test
    public void testWriteLongArray() {
        final long[] data = new long[] { 1, 2, 3, 4};
        Serialiser serialiser = new Packer(buffy -> {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(buffy);
            try {
                assertEquals(1, unpacker.unpackArrayHeader());
                assertEquals(4, unpacker.unpackArrayHeader());
                for (long datum : data) {
                    assertEquals(datum, unpacker.unpackLong());
                }
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }, ByteBuffer.allocate(100));
        serialiser.serialise(data, (x, w) -> w.writeObject(x, s -> null));
        serialiser.flush();
    }

    @Test
    public void testWriteIntArray() {
        final int[] data = new int[] { 1, 2, 3, 4};
        Serialiser serialiser = new Packer(buffy -> {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(buffy);
            try {
                assertEquals(1, unpacker.unpackArrayHeader());
                assertEquals(4, unpacker.unpackArrayHeader());
                for (int datum : data) {
                    assertEquals(datum, unpacker.unpackInt());
                }
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }, ByteBuffer.allocate(100));
        serialiser.serialise(data, (x, w) -> w.writeObject(x, s -> null));
        serialiser.flush();
    }

    @Test
    public void testWriteShortArray() {
        final short[] data = new short[] { 1, 2, 3, 4};
        Serialiser serialiser = new Packer(buffy -> {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(buffy);
            try {
                assertEquals(1, unpacker.unpackArrayHeader());
                assertEquals(4, unpacker.unpackArrayHeader());
                for (short datum : data) {
                    assertEquals(datum, unpacker.unpackInt());
                }
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }, ByteBuffer.allocate(100));
        serialiser.serialise(data, (x, w) -> w.writeObject(x, s -> null));
        serialiser.flush();
    }

    @Test
    public void testWriteLongBoxed() {
        final long data = 1234L;
        Serialiser serialiser = new Packer(buffy -> {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(buffy);
            try {
                assertEquals(1, unpacker.unpackArrayHeader());
                assertEquals(data, unpacker.unpackLong());
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }, ByteBuffer.allocate(100));
        serialiser.serialise(data, (x, w) -> w.writeObject(x, s -> null));
        serialiser.flush();
    }

    @Test
    public void testWriteLongPrimitive() {
        final long data = 1234L;
        Serialiser serialiser = new Packer(buffy -> {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(buffy);
            try {
                assertEquals(1, unpacker.unpackArrayHeader());
                assertEquals(data, unpacker.unpackLong());
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }, ByteBuffer.allocate(100));
        serialiser.serialise(data, (x, w) -> w.writeLong(x));
        serialiser.flush();
    }

    @Test
    public void testWriteIntBoxed() {
        final int data = 1234;
        Serialiser serialiser = new Packer(buffy -> {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(buffy);
            try {
                assertEquals(1, unpacker.unpackArrayHeader());
                assertEquals(data, unpacker.unpackInt());
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }, ByteBuffer.allocate(100));
        serialiser.serialise(data, (x, w) -> w.writeObject(x, s -> null));
        serialiser.flush();
    }

    @Test
    public void testWriteIntPrimitive() {
        final int data = 1234;
        Serialiser serialiser = new Packer(buffy -> {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(buffy);
            try {
                assertEquals(1, unpacker.unpackArrayHeader());
                assertEquals(data, unpacker.unpackInt());
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }, ByteBuffer.allocate(100));
        serialiser.serialise(data, (x, w) -> w.writeInt(x));
        serialiser.flush();
    }

    @Test
    public void testWriteShortBoxed() {
        final short data = 1234;
        Serialiser serialiser = new Packer(buffy -> {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(buffy);
            try {
                assertEquals(1, unpacker.unpackArrayHeader());
                assertEquals(data, unpacker.unpackInt());
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }, ByteBuffer.allocate(100));
        serialiser.serialise(data, (x, w) -> w.writeObject(x, s -> null));
        serialiser.flush();
    }

    @Test
    public void testUnknownObject() {
        final Object data = Codec.INSTANCE;
        Serialiser serialiser = new Packer(buffy -> {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(buffy);
            try {
                assertEquals(1, unpacker.unpackArrayHeader());
                assertEquals(data.toString(), unpacker.unpackString());
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }, ByteBuffer.allocate(100));
        serialiser.serialise(data, (x, w) -> w.writeObject(x, s -> null));
        serialiser.flush();
    }

    @Test
    public void testWriteObjectArray() {
        final Object[] data = new Object[] { "foo", "bar"};
        Serialiser serialiser = new Packer(buffy -> {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(buffy);
            try {
                assertEquals(1, unpacker.unpackArrayHeader());
                assertEquals(data.length, unpacker.unpackArrayHeader());
                assertEquals(data[0].toString(), unpacker.unpackString());
                assertEquals(data[1].toString(), unpacker.unpackString());
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }, ByteBuffer.allocate(100));
        serialiser.serialise(data, (x, w) -> w.writeObject(x, s -> null));
        serialiser.flush();
    }


}
