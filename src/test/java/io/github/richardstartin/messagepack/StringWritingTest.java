package io.github.richardstartin.messagepack;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class StringWritingTest {

    private static final EncodingCache NO_CACHE = EncodingCachingStrategies.none();

    private static final EncodingCache CACHE =
            EncodingCachingStrategies.memoise(s -> ((String)s).getBytes(StandardCharsets.UTF_8));

    private final List<Map<String, String>> maps;
    private final ByteBuffer buffer = ByteBuffer.allocate(10 << 10);


    public StringWritingTest(List<Map<String, String>> maps) {
        this.maps = maps;
    }

    @Parameterized.Parameters
    public static Object[][] maps() {
        return new Object[][]{
                {
                        Arrays.asList(
                                new HashMap<String, String>() {{
                                    put("english", "buh-bye");
                                    put("foo", "bar");
                                }},
                                new HashMap<>() {{
                                    put("german", "Tchuß");
                                    put("foo", "bar");
                                }},
                                new HashMap<>() {{
                                    put("hani", "道");
                                    put("foo", "bar");
                                }},
                                new HashMap<>() {{
                                    put("hani", "道道道");
                                    put("foo", "bar");
                                }}),


                },
                {
                        Arrays.asList(
                                new HashMap<String, String>() {{
                                    put("123456789012390-2394-3", "alshjdhlasjhLKASJKLDAHsdlkAHSDKLJAHsdklHASDKSa");
                                    put("foo", "bar");
                                }},
                                new HashMap<>() {{
                                    put("123456789012390-2394-3", "Staßenschilder");
                                    put("foo", "bar");
                                }},
                                new HashMap<>() {{
                                    put("hani", "道可道非常道名可名非常名");
                                    put("foo", "bar");
                                }})

                },
                {
                        Arrays.asList(
                                new HashMap<>() {{
                                    put("123456789012390-2394-3", "ßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßßß");
                                    put("foo", "bar");
                                }},
                                new HashMap<>() {{
                                    put("hani", "道可道非常道名可名非常名名名名名名名名名名名");
                                    put("foo", "bar");
                                }})

                },
                {
                        Arrays.asList(
                                new HashMap<>() {{
                                    put("emoji", "\uD83D\uDC4D");
                                    put("foo", "bar");
                                }},
                                new HashMap<>() {{
                                    put("emoji", "\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D\uD83D\uDC4D");
                                    put("foo", "bar");
                                }})

                }
        };
    }

    @Test
    public void testSerialiseTextMapWithCache() {
        Packer packer = new Packer(this::testBufferContents, buffer);
        for (Map<String, String> map : maps) {
            packer.serialise(map, (m, p) -> p.writeMap(m, CACHE));
        }
        packer.flush();
    }

    @Test
    public void testSerialiseTextMapWithoutCache() {
        Packer packer = new Packer(this::testBufferContents, buffer);
        for (Map<String, String> map : maps) {
            packer.serialise(map, (m, p) -> p.writeMap(m, NO_CACHE));
        }
        packer.flush();
    }

    @After
    public void resetBuffer() {
        buffer.position(0);
        buffer.limit(buffer.capacity());
    }


    private void testBufferContents(ByteBuffer buffer) {
        try {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(buffer);
            int length = unpacker.unpackArrayHeader();
            assertEquals(maps.size(), length);
            for (Map<String, String> map : maps) {
                int mapSize = unpacker.unpackMapHeader();
                assertEquals(map.size(), mapSize);
                int pos = 0;
                while (pos++ < mapSize) {
                    String key = unpacker.unpackString();
                    String expectedValue = map.get(key);
                    assertNotNull(expectedValue);
                    assertEquals(expectedValue, unpacker.unpackString());
                }
            }
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

    }
}
