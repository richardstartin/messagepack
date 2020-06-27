package io.github.richardstartin.messagepack;

import org.junit.Test;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PackerTest {

    static class Foo {
        long id1;
        long id2;
        String name;
        int[] values;
        Map<String, Object> tags;

        static Foo create() {
            Foo foo = new Foo();
            foo.id1 = ThreadLocalRandom.current().nextLong();
            foo.id2 = ThreadLocalRandom.current().nextLong();
            foo.name = UUID.randomUUID().toString();
            foo.values = new int[10];
            Arrays.fill(foo.values, ThreadLocalRandom.current().nextInt());
            foo.tags = new HashMap<>();
            foo.tags.put("foo", 9f);
            foo.tags.put("bar", 9d);
            foo.tags.put("qux", "tag");
            foo.tags.put("list", Collections.singletonList("element"));
            return foo;
        }
    }

    Map<? extends CharSequence, byte[]> constantPool = new HashMap<>() {{
        put("foo", "foo".getBytes(StandardCharsets.UTF_8));
        put("id1", "id1".getBytes(StandardCharsets.UTF_8));
    }};
    Function<CharSequence, byte[]> toBytes = constantPool::get;
    @Test
    public void testWriteMessage() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        Packer packer = new Packer(Codec.INSTANCE, buffy -> {}, buffer);
        Foo message = Foo.create();
        packer.serialise(message, (data, p) -> {
            p.writeString("id1", toBytes);
            p.writeLong(data.id1);
            p.writeString("id2", toBytes);
            p.writeLong(data.id2);
            p.writeString("name", toBytes);
            p.writeString(data.name, toBytes);
            p.writeString("values", toBytes);
            p.writeObject(data.values, toBytes);
            p.writeString("tags", toBytes);
            p.writeMap(data.tags, toBytes);
        });
        buffer.flip();
        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(buffer);
        assertEquals("id1", unpacker.unpackString());
        assertEquals(message.id1, unpacker.unpackLong());
        assertEquals("id2", unpacker.unpackString());
        assertEquals(message.id2, unpacker.unpackLong());
        assertEquals("name", unpacker.unpackString());
        assertEquals(message.name, unpacker.unpackString());
        assertEquals("values", unpacker.unpackString());
        int length = unpacker.unpackArrayHeader();
        assertEquals(message.values.length, length);
        for (int i : message.values) {
            assertEquals(i, unpacker.unpackInt());
        }
        assertEquals("tags", unpacker.unpackString());
        int mapHeader = unpacker.unpackMapHeader();
        assertEquals(message.tags.size(), mapHeader);
        for  (int i = 0; i < mapHeader; ++i) {
            String key = unpacker.unpackString();
            Object expected = message.tags.get(key);
            assertNotNull(expected);
            if (expected instanceof Float) {
                assertEquals((Float)expected, unpacker.unpackFloat(), 0.0001);
            } else if (expected instanceof Double) {
                assertEquals((Double) expected, unpacker.unpackDouble(), 0.0001);
            } else if (expected instanceof List) {
                List<String> l = (List<String>) expected;
                assertEquals(l.size(), unpacker.unpackArrayHeader());
                for (String element : l) {
                    assertEquals(element, unpacker.unpackString());
                }
            } else if (expected instanceof String) {
                assertEquals(expected, unpacker.unpackString());
            }
        }
    }




}