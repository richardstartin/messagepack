# messagepack

Maps streams of objects to messagepack arrays without allocation. Faster and lighter-weight than msgpack-java.


```java
var buffer = ByteBuffer.allocate(BUFFER_SIZE);
var serialiser = new Packer(b -> send(b), buffer);
for (MyMetric o : myMetrics) {
  serialiser.serialise(o, (x, w) -> {
     w.writeString("id");
     w.writeLong(o.getId());
     w.writeString("name");
     w.writeString(o.getName());
     w.writeString("value");
     w.writeDouble(o.getValue());
     w.writeString("tags");
     w.writeMap(o.getTags());
});
serialiser.flush();

```
