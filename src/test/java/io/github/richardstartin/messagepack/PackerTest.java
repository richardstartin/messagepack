package io.github.richardstartin.messagepack;

import org.junit.Test;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class PackerTest {

    @Test(expected = BufferOverflowException.class)
    public void testOverflow() {
        Packer packer = new Packer(buffer -> {}, ByteBuffer.allocate(25));
        packer.serialise(new HashMap<>() {{
            put("foo", "abcdefghijklmnopqrstuvwxyz");
        }}, (Mapper<HashMap<? extends Object, ? extends Object>>) (data, packer1) -> packer1.writeObject(data, x -> null));
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
}
