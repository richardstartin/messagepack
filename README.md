# messagepack

Maps streams of objects to messagepack arrays without allocation. Faster and lighter-weight than msgpack-java.


```java
var buffer = ByteBuffer.allocate(BUFFER_SIZE);
var serialiser = new Packer(b -> send(b), buffer);
for (MyMetric o : myMetrics) {
  serialiser.serialise(o, (x, w) -> {
     w.writeString("id");
     w.writeLong(x.getId());
     w.writeString("name");
     w.writeString(x.getName());
     w.writeString("value");
     w.writeDouble(x.getValue());
     w.writeString("tags");
     w.writeMap(x.getTags());
});
serialiser.flush();

```
