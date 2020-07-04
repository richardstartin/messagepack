# messagepack

Maps streams of objects to messagepack arrays without allocation. Faster and lighter-weight than msgpack-java.


```java
var buffer = ByteBuffer.allocate(BUFFER_SIZE);
var serialiser = new Packer(b -> send(b), buffer);
for (MyMetric myMetric : myMetrics) {
  serialiser.serialise(myMetric, (MyMetric metric, Writable writable) -> {
     writable.writeString("id");
     writable.writeLong(metric.getId());
     writable.writeString("name");
     writable.writeString(metric.getName());
     writable.writeString("value");
     writable.writeDouble(metric.getValue());
     writable.writeString("tags");
     writable.writeMap(metric.getTags());
});
serialiser.flush();

```
