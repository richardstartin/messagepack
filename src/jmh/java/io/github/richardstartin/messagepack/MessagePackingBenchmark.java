package io.github.richardstartin.messagepack;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.buffer.ArrayBufferOutput;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MessagePackingBenchmark {

    /*

    Benchmark                                                                (bufferSizeMB)  (cachePercentage)  (direct)  (keySize)  (size)   Mode  Cnt       Score      Error   Units
MessagePackingBenchmark.messagePacker                                                 1                N/A       N/A          4    1024  thrpt    5      27.461 ±    1.866  ops/ms
MessagePackingBenchmark.messagePacker:·gc.alloc.rate                                  1                N/A       N/A          4    1024  thrpt    5     777.452 ±   52.287  MB/sec
MessagePackingBenchmark.messagePacker:·gc.alloc.rate.norm                             1                N/A       N/A          4    1024  thrpt    5   44805.220 ±  102.063    B/op
MessagePackingBenchmark.messagePacker:·gc.churn.Eden_Space                            1                N/A       N/A          4    1024  thrpt    5     775.093 ±   91.964  MB/sec
MessagePackingBenchmark.messagePacker:·gc.churn.Eden_Space.norm                       1                N/A       N/A          4    1024  thrpt    5   44680.524 ± 6171.837    B/op
MessagePackingBenchmark.messagePacker:·gc.churn.Survivor_Space                        1                N/A       N/A          4    1024  thrpt    5       0.531 ±    1.137  MB/sec
MessagePackingBenchmark.messagePacker:·gc.churn.Survivor_Space.norm                   1                N/A       N/A          4    1024  thrpt    5      30.517 ±   65.413    B/op
MessagePackingBenchmark.messagePacker:·gc.count                                       1                N/A       N/A          4    1024  thrpt    5      88.000             counts
MessagePackingBenchmark.messagePacker:·gc.time                                        1                N/A       N/A          4    1024  thrpt    5      24.000                 ms
MessagePackingBenchmark.messagePacker                                                 1                N/A       N/A          8    1024  thrpt    5      26.277 ±    0.917  ops/ms
MessagePackingBenchmark.messagePacker:·gc.alloc.rate                                  1                N/A       N/A          8    1024  thrpt    5     834.830 ±   33.595  MB/sec
MessagePackingBenchmark.messagePacker:·gc.alloc.rate.norm                             1                N/A       N/A          8    1024  thrpt    5   50216.421 ±   77.485    B/op
MessagePackingBenchmark.messagePacker:·gc.churn.Eden_Space                            1                N/A       N/A          8    1024  thrpt    5     837.359 ±    6.815  MB/sec
MessagePackingBenchmark.messagePacker:·gc.churn.Eden_Space.norm                       1                N/A       N/A          8    1024  thrpt    5   50372.402 ± 1792.988    B/op
MessagePackingBenchmark.messagePacker:·gc.churn.Survivor_Space                        1                N/A       N/A          8    1024  thrpt    5       3.982 ±    0.028  MB/sec
MessagePackingBenchmark.messagePacker:·gc.churn.Survivor_Space.norm                   1                N/A       N/A          8    1024  thrpt    5     239.528 ±    8.911    B/op
MessagePackingBenchmark.messagePacker:·gc.count                                       1                N/A       N/A          8    1024  thrpt    5      95.000             counts
MessagePackingBenchmark.messagePacker:·gc.time                                        1                N/A       N/A          8    1024  thrpt    5      24.000                 ms
MessagePackingBenchmark.messagePacker                                                 1                N/A       N/A         16    1024  thrpt    5      22.451 ±    1.702  ops/ms
MessagePackingBenchmark.messagePacker:·gc.alloc.rate                                  1                N/A       N/A         16    1024  thrpt    5     977.997 ±   55.627  MB/sec
MessagePackingBenchmark.messagePacker:·gc.alloc.rate.norm                             1                N/A       N/A         16    1024  thrpt    5   68987.933 ±   83.239    B/op
MessagePackingBenchmark.messagePacker:·gc.churn.Eden_Space                            1                N/A       N/A         16    1024  thrpt    5     985.606 ±  142.671  MB/sec
MessagePackingBenchmark.messagePacker:·gc.churn.Eden_Space.norm                       1                N/A       N/A         16    1024  thrpt    5   69508.753 ± 6783.588    B/op
MessagePackingBenchmark.messagePacker:·gc.churn.Survivor_Space                        1                N/A       N/A         16    1024  thrpt    5       5.698 ±    2.395  MB/sec
MessagePackingBenchmark.messagePacker:·gc.churn.Survivor_Space.norm                   1                N/A       N/A         16    1024  thrpt    5     402.194 ±  177.217    B/op
MessagePackingBenchmark.messagePacker:·gc.count                                       1                N/A       N/A         16    1024  thrpt    5     112.000             counts
MessagePackingBenchmark.messagePacker:·gc.time                                        1                N/A       N/A         16    1024  thrpt    5      25.000                 ms
MessagePackingBenchmark.messagePacker                                                 1                N/A       N/A         32    1024  thrpt    5      23.634 ±    2.115  ops/ms
MessagePackingBenchmark.messagePacker:·gc.alloc.rate                                  1                N/A       N/A         32    1024  thrpt    5    1605.979 ±  130.804  MB/sec
MessagePackingBenchmark.messagePacker:·gc.alloc.rate.norm                             1                N/A       N/A         32    1024  thrpt    5  107472.137 ±   57.313    B/op
MessagePackingBenchmark.messagePacker:·gc.churn.Eden_Space                            1                N/A       N/A         32    1024  thrpt    5    1609.098 ±  177.704  MB/sec
MessagePackingBenchmark.messagePacker:·gc.churn.Eden_Space.norm                       1                N/A       N/A         32    1024  thrpt    5  107672.889 ± 5705.657    B/op
MessagePackingBenchmark.messagePacker:·gc.churn.Survivor_Space                        1                N/A       N/A         32    1024  thrpt    5       0.004 ±    0.007  MB/sec
MessagePackingBenchmark.messagePacker:·gc.churn.Survivor_Space.norm                   1                N/A       N/A         32    1024  thrpt    5       0.279 ±    0.451    B/op
MessagePackingBenchmark.messagePacker:·gc.count                                       1                N/A       N/A         32    1024  thrpt    5     182.000             counts
MessagePackingBenchmark.messagePacker:·gc.time                                        1                N/A       N/A         32    1024  thrpt    5      26.000                 ms
MessagePackingBenchmark.packer                                                        1                0.0      true          4    1024  thrpt    5      32.034 ±    0.870  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                0.0      true          4    1024  thrpt    5       1.300 ±    0.040  MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                0.0      true          4    1024  thrpt    5      64.013 ±    0.002    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                0.0      true          4    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                0.0      true          8    1024  thrpt    5      26.487 ±    0.185  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                0.0      true          8    1024  thrpt    5       1.075 ±    0.007  MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                0.0      true          8    1024  thrpt    5      64.016 ±    0.002    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                0.0      true          8    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                0.0      true         16    1024  thrpt    5      22.953 ±    0.229  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                0.0      true         16    1024  thrpt    5       0.930 ±    0.016  MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                0.0      true         16    1024  thrpt    5      64.018 ±    0.003    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                0.0      true         16    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                0.0      true         32    1024  thrpt    5      16.422 ±    4.112  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                0.0      true         32    1024  thrpt    5       0.665 ±    0.174  MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                0.0      true         32    1024  thrpt    5      64.027 ±    0.011    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                0.0      true         32    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                0.0     false          4    1024  thrpt    5      28.027 ±    3.675  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                0.0     false          4    1024  thrpt    5      ≈ 10⁻⁴             MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                0.0     false          4    1024  thrpt    5       0.015 ±    0.002    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                0.0     false          4    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                0.0     false          8    1024  thrpt    5      28.640 ±    0.081  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                0.0     false          8    1024  thrpt    5       0.871 ±    0.009  MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                0.0     false          8    1024  thrpt    5      48.014 ±    0.002    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                0.0     false          8    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                0.0     false         16    1024  thrpt    5      22.984 ±    0.202  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                0.0     false         16    1024  thrpt    5       0.699 ±    0.010  MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                0.0     false         16    1024  thrpt    5      48.018 ±    0.003    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                0.0     false         16    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                0.0     false         32    1024  thrpt    5      17.604 ±    0.269  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                0.0     false         32    1024  thrpt    5       0.535 ±    0.009  MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                0.0     false         32    1024  thrpt    5      48.025 ±    0.010    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                0.0     false         32    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                0.1      true          4    1024  thrpt    5      28.297 ±    2.104  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                0.1      true          4    1024  thrpt    5       1.144 ±    0.078  MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                0.1      true          4    1024  thrpt    5      64.015 ±    0.002    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                0.1      true          4    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                0.1      true          8    1024  thrpt    5      22.638 ±    8.797  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                0.1      true          8    1024  thrpt    5       0.916 ±    0.377  MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                0.1      true          8    1024  thrpt    5      64.018 ±    0.008    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                0.1      true          8    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                0.1      true         16    1024  thrpt    5      23.389 ±    0.348  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                0.1      true         16    1024  thrpt    5       0.949 ±    0.014  MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                0.1      true         16    1024  thrpt    5      64.018 ±    0.002    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                0.1      true         16    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                0.1      true         32    1024  thrpt    5      18.798 ±    1.285  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                0.1      true         32    1024  thrpt    5       0.763 ±    0.051  MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                0.1      true         32    1024  thrpt    5      64.024 ±    0.014    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                0.1      true         32    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                0.1     false          4    1024  thrpt    5      25.863 ±    0.420  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                0.1     false          4    1024  thrpt    5       0.786 ±    0.015  MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                0.1     false          4    1024  thrpt    5      48.016 ±    0.002    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                0.1     false          4    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                0.1     false          8    1024  thrpt    5      23.988 ±    0.955  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                0.1     false          8    1024  thrpt    5       0.730 ±    0.031  MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                0.1     false          8    1024  thrpt    5      48.018 ±    0.003    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                0.1     false          8    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                0.1     false         16    1024  thrpt    5      21.333 ±    0.971  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                0.1     false         16    1024  thrpt    5      ≈ 10⁻⁴             MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                0.1     false         16    1024  thrpt    5       0.019 ±    0.003    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                0.1     false         16    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                0.1     false         32    1024  thrpt    5      16.180 ±    1.690  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                0.1     false         32    1024  thrpt    5       0.492 ±    0.052  MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                0.1     false         32    1024  thrpt    5      48.027 ±    0.012    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                0.1     false         32    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                0.2      true          4    1024  thrpt    5      30.287 ±    0.838  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                0.2      true          4    1024  thrpt    5       1.229 ±    0.035  MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                0.2      true          4    1024  thrpt    5      64.014 ±    0.002    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                0.2      true          4    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                0.2      true          8    1024  thrpt    5      23.961 ±    1.136  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                0.2      true          8    1024  thrpt    5       0.971 ±    0.052  MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                0.2      true          8    1024  thrpt    5      64.017 ±    0.003    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                0.2      true          8    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                0.2      true         16    1024  thrpt    5      22.890 ±    0.327  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                0.2      true         16    1024  thrpt    5       0.926 ±    0.036  MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                0.2      true         16    1024  thrpt    5      64.018 ±    0.003    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                0.2      true         16    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                0.2      true         32    1024  thrpt    5      21.013 ±    0.695  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                0.2      true         32    1024  thrpt    5       0.852 ±    0.029  MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                0.2      true         32    1024  thrpt    5      64.020 ±    0.003    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                0.2      true         32    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                0.2     false          4    1024  thrpt    5      27.757 ±    1.331  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                0.2     false          4    1024  thrpt    5      ≈ 10⁻⁴             MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                0.2     false          4    1024  thrpt    5       0.015 ±    0.002    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                0.2     false          4    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                0.2     false          8    1024  thrpt    5      24.789 ±    1.160  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                0.2     false          8    1024  thrpt    5       0.752 ±    0.053  MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                0.2     false          8    1024  thrpt    5      48.017 ±    0.003    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                0.2     false          8    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                0.2     false         16    1024  thrpt    5      22.075 ±    6.937  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                0.2     false         16    1024  thrpt    5      ≈ 10⁻⁴             MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                0.2     false         16    1024  thrpt    5       0.019 ±    0.009    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                0.2     false         16    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                0.2     false         32    1024  thrpt    5      16.559 ±    0.607  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                0.2     false         32    1024  thrpt    5       0.504 ±    0.018  MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                0.2     false         32    1024  thrpt    5      48.026 ±    0.015    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                0.2     false         32    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                1.0      true          4    1024  thrpt    5      38.613 ±    1.013  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                1.0      true          4    1024  thrpt    5       1.567 ±    0.039  MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                1.0      true          4    1024  thrpt    5      64.011 ±    0.002    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                1.0      true          4    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                1.0      true          8    1024  thrpt    5      34.768 ±    2.299  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                1.0      true          8    1024  thrpt    5       1.407 ±    0.087  MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                1.0      true          8    1024  thrpt    5      64.012 ±    0.001    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                1.0      true          8    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                1.0      true         16    1024  thrpt    5      32.330 ±    0.984  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                1.0      true         16    1024  thrpt    5       1.312 ±    0.046  MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                1.0      true         16    1024  thrpt    5      64.013 ±    0.002    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                1.0      true         16    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                1.0      true         32    1024  thrpt    5      30.935 ±    5.434  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                1.0      true         32    1024  thrpt    5       1.255 ±    0.221  MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                1.0      true         32    1024  thrpt    5      64.013 ±    0.003    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                1.0      true         32    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                1.0     false          4    1024  thrpt    5      34.921 ±    1.634  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                1.0     false          4    1024  thrpt    5      ≈ 10⁻⁴             MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                1.0     false          4    1024  thrpt    5       0.012 ±    0.002    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                1.0     false          4    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                1.0     false          8    1024  thrpt    5      34.205 ±    3.084  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                1.0     false          8    1024  thrpt    5      ≈ 10⁻⁴             MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                1.0     false          8    1024  thrpt    5       0.012 ±    0.001    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                1.0     false          8    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                1.0     false         16    1024  thrpt    5      33.131 ±    1.528  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                1.0     false         16    1024  thrpt    5      ≈ 10⁻⁴             MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                1.0     false         16    1024  thrpt    5       0.013 ±    0.001    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                1.0     false         16    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.packer                                                        1                1.0     false         32    1024  thrpt    5      32.474 ±    3.942  ops/ms
MessagePackingBenchmark.packer:·gc.alloc.rate                                         1                1.0     false         32    1024  thrpt    5      ≈ 10⁻⁴             MB/sec
MessagePackingBenchmark.packer:·gc.alloc.rate.norm                                    1                1.0     false         32    1024  thrpt    5       0.013 ±    0.003    B/op
MessagePackingBenchmark.packer:·gc.count                                              1                1.0     false         32    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8MessagePacker                                             1                N/A       N/A          4    1024  thrpt    5      37.173 ±    1.126  ops/ms
MessagePackingBenchmark.utf8MessagePacker:·gc.alloc.rate                              1                N/A       N/A          4    1024  thrpt    5     474.822 ±   20.276  MB/sec
MessagePackingBenchmark.utf8MessagePacker:·gc.alloc.rate.norm                         1                N/A       N/A          4    1024  thrpt    5   20224.749 ±   38.161    B/op
MessagePackingBenchmark.utf8MessagePacker:·gc.churn.Eden_Space                        1                N/A       N/A          4    1024  thrpt    5     474.599 ±   71.687  MB/sec
MessagePackingBenchmark.utf8MessagePacker:·gc.churn.Eden_Space.norm                   1                N/A       N/A          4    1024  thrpt    5   20216.776 ± 3117.248    B/op
MessagePackingBenchmark.utf8MessagePacker:·gc.churn.Survivor_Space                    1                N/A       N/A          4    1024  thrpt    5       0.003 ±    0.007  MB/sec
MessagePackingBenchmark.utf8MessagePacker:·gc.churn.Survivor_Space.norm               1                N/A       N/A          4    1024  thrpt    5       0.120 ±    0.311    B/op
MessagePackingBenchmark.utf8MessagePacker:·gc.count                                   1                N/A       N/A          4    1024  thrpt    5      54.000             counts
MessagePackingBenchmark.utf8MessagePacker:·gc.time                                    1                N/A       N/A          4    1024  thrpt    5       9.000                 ms
MessagePackingBenchmark.utf8MessagePacker                                             1                N/A       N/A          8    1024  thrpt    5      34.920 ±    1.845  ops/ms
MessagePackingBenchmark.utf8MessagePacker:·gc.alloc.rate                              1                N/A       N/A          8    1024  thrpt    5     566.858 ±   32.434  MB/sec
MessagePackingBenchmark.utf8MessagePacker:·gc.alloc.rate.norm                         1                N/A       N/A          8    1024  thrpt    5   25638.841 ±   51.599    B/op
MessagePackingBenchmark.utf8MessagePacker:·gc.churn.Eden_Space                        1                N/A       N/A          8    1024  thrpt    5     572.730 ±    4.661  MB/sec
MessagePackingBenchmark.utf8MessagePacker:·gc.churn.Eden_Space.norm                   1                N/A       N/A          8    1024  thrpt    5   25908.681 ± 1384.253    B/op
MessagePackingBenchmark.utf8MessagePacker:·gc.churn.Survivor_Space                    1                N/A       N/A          8    1024  thrpt    5       0.003 ±    0.007  MB/sec
MessagePackingBenchmark.utf8MessagePacker:·gc.churn.Survivor_Space.norm               1                N/A       N/A          8    1024  thrpt    5       0.124 ±    0.315    B/op
MessagePackingBenchmark.utf8MessagePacker:·gc.count                                   1                N/A       N/A          8    1024  thrpt    5      65.000             counts
MessagePackingBenchmark.utf8MessagePacker:·gc.time                                    1                N/A       N/A          8    1024  thrpt    5      11.000                 ms
MessagePackingBenchmark.utf8MessagePacker                                             1                N/A       N/A         16    1024  thrpt    5      34.115 ±    1.959  ops/ms
MessagePackingBenchmark.utf8MessagePacker:·gc.alloc.rate                              1                N/A       N/A         16    1024  thrpt    5     782.186 ±   43.447  MB/sec
MessagePackingBenchmark.utf8MessagePacker:·gc.alloc.rate.norm                         1                N/A       N/A         16    1024  thrpt    5   36218.560 ±   76.103    B/op
MessagePackingBenchmark.utf8MessagePacker:·gc.churn.Eden_Space                        1                N/A       N/A         16    1024  thrpt    5     784.073 ±   77.159  MB/sec
MessagePackingBenchmark.utf8MessagePacker:·gc.churn.Eden_Space.norm                   1                N/A       N/A         16    1024  thrpt    5   36306.626 ± 3136.570    B/op
MessagePackingBenchmark.utf8MessagePacker:·gc.churn.Survivor_Space                    1                N/A       N/A         16    1024  thrpt    5       0.004 ±    0.007  MB/sec
MessagePackingBenchmark.utf8MessagePacker:·gc.churn.Survivor_Space.norm               1                N/A       N/A         16    1024  thrpt    5       0.202 ±    0.327    B/op
MessagePackingBenchmark.utf8MessagePacker:·gc.count                                   1                N/A       N/A         16    1024  thrpt    5      89.000             counts
MessagePackingBenchmark.utf8MessagePacker:·gc.time                                    1                N/A       N/A         16    1024  thrpt    5      15.000                 ms
MessagePackingBenchmark.utf8MessagePacker                                             1                N/A       N/A         32    1024  thrpt    5      32.835 ±    1.349  ops/ms
MessagePackingBenchmark.utf8MessagePacker:·gc.alloc.rate                              1                N/A       N/A         32    1024  thrpt    5    1212.268 ±   51.687  MB/sec
MessagePackingBenchmark.utf8MessagePacker:·gc.alloc.rate.norm                         1                N/A       N/A         32    1024  thrpt    5   58318.461 ±   29.614    B/op
MessagePackingBenchmark.utf8MessagePacker:·gc.churn.Eden_Space                        1                N/A       N/A         32    1024  thrpt    5    1215.673 ±   86.436  MB/sec
MessagePackingBenchmark.utf8MessagePacker:·gc.churn.Eden_Space.norm                   1                N/A       N/A         32    1024  thrpt    5   58492.479 ± 5771.298    B/op
MessagePackingBenchmark.utf8MessagePacker:·gc.churn.Survivor_Space                    1                N/A       N/A         32    1024  thrpt    5       0.004 ±    0.007  MB/sec
MessagePackingBenchmark.utf8MessagePacker:·gc.churn.Survivor_Space.norm               1                N/A       N/A         32    1024  thrpt    5       0.201 ±    0.352    B/op
MessagePackingBenchmark.utf8MessagePacker:·gc.count                                   1                N/A       N/A         32    1024  thrpt    5     138.000             counts
MessagePackingBenchmark.utf8MessagePacker:·gc.time                                    1                N/A       N/A         32    1024  thrpt    5      20.000                 ms
MessagePackingBenchmark.utf8Packer                                                    1                0.0      true          4    1024  thrpt    5      48.607 ±    0.537  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                0.0      true          4    1024  thrpt    5       1.973 ±    0.024  MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                0.0      true          4    1024  thrpt    5      64.009 ±    0.001    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                0.0      true          4    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                0.0      true          8    1024  thrpt    5      48.667 ±    0.758  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                0.0      true          8    1024  thrpt    5       1.974 ±    0.036  MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                0.0      true          8    1024  thrpt    5      64.009 ±    0.001    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                0.0      true          8    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                0.0      true         16    1024  thrpt    5      44.882 ±    0.917  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                0.0      true         16    1024  thrpt    5       1.821 ±    0.038  MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                0.0      true         16    1024  thrpt    5      64.009 ±    0.001    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                0.0      true         16    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                0.0      true         32    1024  thrpt    5      43.751 ±    0.579  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                0.0      true         32    1024  thrpt    5       1.775 ±    0.025  MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                0.0      true         32    1024  thrpt    5      64.009 ±    0.001    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                0.0      true         32    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                0.0     false          4    1024  thrpt    5      47.923 ±    0.854  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                0.0     false          4    1024  thrpt    5      ≈ 10⁻⁴             MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                0.0     false          4    1024  thrpt    5       0.009 ±    0.001    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                0.0     false          4    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                0.0     false          8    1024  thrpt    5      50.022 ±    0.761  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                0.0     false          8    1024  thrpt    5      ≈ 10⁻⁴             MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                0.0     false          8    1024  thrpt    5       0.008 ±    0.001    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                0.0     false          8    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                0.0     false         16    1024  thrpt    5      45.802 ±    1.245  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                0.0     false         16    1024  thrpt    5      ≈ 10⁻⁴             MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                0.0     false         16    1024  thrpt    5       0.009 ±    0.001    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                0.0     false         16    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                0.0     false         32    1024  thrpt    5      46.514 ±    0.998  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                0.0     false         32    1024  thrpt    5      ≈ 10⁻⁴             MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                0.0     false         32    1024  thrpt    5       0.009 ±    0.001    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                0.0     false         32    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                0.1      true          4    1024  thrpt    5      48.331 ±    0.769  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                0.1      true          4    1024  thrpt    5       1.961 ±    0.031  MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                0.1      true          4    1024  thrpt    5      64.009 ±    0.001    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                0.1      true          4    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                0.1      true          8    1024  thrpt    5      46.746 ±    0.580  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                0.1      true          8    1024  thrpt    5       1.896 ±    0.028  MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                0.1      true          8    1024  thrpt    5      64.009 ±    0.001    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                0.1      true          8    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                0.1      true         16    1024  thrpt    5      43.741 ±    0.544  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                0.1      true         16    1024  thrpt    5       1.775 ±    0.021  MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                0.1      true         16    1024  thrpt    5      64.010 ±    0.002    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                0.1      true         16    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                0.1      true         32    1024  thrpt    5      44.502 ±    1.326  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                0.1      true         32    1024  thrpt    5       1.806 ±    0.052  MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                0.1      true         32    1024  thrpt    5      64.009 ±    0.001    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                0.1      true         32    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                0.1     false          4    1024  thrpt    5      49.198 ±    0.901  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                0.1     false          4    1024  thrpt    5      ≈ 10⁻⁴             MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                0.1     false          4    1024  thrpt    5       0.008 ±    0.001    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                0.1     false          4    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                0.1     false          8    1024  thrpt    5      50.708 ±    0.576  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                0.1     false          8    1024  thrpt    5      ≈ 10⁻⁴             MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                0.1     false          8    1024  thrpt    5       0.008 ±    0.001    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                0.1     false          8    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                0.1     false         16    1024  thrpt    5      45.382 ±    0.735  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                0.1     false         16    1024  thrpt    5      ≈ 10⁻⁴             MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                0.1     false         16    1024  thrpt    5       0.009 ±    0.001    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                0.1     false         16    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                0.1     false         32    1024  thrpt    5      46.773 ±    2.012  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                0.1     false         32    1024  thrpt    5      ≈ 10⁻⁴             MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                0.1     false         32    1024  thrpt    5       0.009 ±    0.001    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                0.1     false         32    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                0.2      true          4    1024  thrpt    5      54.856 ±    1.119  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                0.2      true          4    1024  thrpt    5       2.225 ±    0.046  MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                0.2      true          4    1024  thrpt    5      64.008 ±    0.001    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                0.2      true          4    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                0.2      true          8    1024  thrpt    5      46.804 ±    0.572  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                0.2      true          8    1024  thrpt    5       1.899 ±    0.020  MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                0.2      true          8    1024  thrpt    5      64.009 ±    0.001    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                0.2      true          8    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                0.2      true         16    1024  thrpt    5      43.556 ±    0.477  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                0.2      true         16    1024  thrpt    5       1.767 ±    0.025  MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                0.2      true         16    1024  thrpt    5      64.010 ±    0.002    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                0.2      true         16    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                0.2      true         32    1024  thrpt    5      44.321 ±    0.901  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                0.2      true         32    1024  thrpt    5       1.798 ±    0.039  MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                0.2      true         32    1024  thrpt    5      64.009 ±    0.001    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                0.2      true         32    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                0.2     false          4    1024  thrpt    5      48.833 ±    0.751  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                0.2     false          4    1024  thrpt    5      ≈ 10⁻⁴             MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                0.2     false          4    1024  thrpt    5       0.009 ±    0.001    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                0.2     false          4    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                0.2     false          8    1024  thrpt    5      50.652 ±    1.235  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                0.2     false          8    1024  thrpt    5      ≈ 10⁻⁴             MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                0.2     false          8    1024  thrpt    5       0.008 ±    0.001    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                0.2     false          8    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                0.2     false         16    1024  thrpt    5      45.059 ±    1.283  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                0.2     false         16    1024  thrpt    5      ≈ 10⁻⁴             MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                0.2     false         16    1024  thrpt    5       0.009 ±    0.001    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                0.2     false         16    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                0.2     false         32    1024  thrpt    5      46.481 ±    1.373  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                0.2     false         32    1024  thrpt    5      ≈ 10⁻⁴             MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                0.2     false         32    1024  thrpt    5       0.009 ±    0.001    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                0.2     false         32    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                1.0      true          4    1024  thrpt    5      56.137 ±    1.551  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                1.0      true          4    1024  thrpt    5       2.277 ±    0.079  MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                1.0      true          4    1024  thrpt    5      64.007 ±    0.001    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                1.0      true          4    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                1.0      true          8    1024  thrpt    5      48.115 ±    1.167  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                1.0      true          8    1024  thrpt    5       1.953 ±    0.043  MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                1.0      true          8    1024  thrpt    5      64.009 ±    0.002    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                1.0      true          8    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                1.0      true         16    1024  thrpt    5      44.190 ±    0.891  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                1.0      true         16    1024  thrpt    5       1.793 ±    0.038  MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                1.0      true         16    1024  thrpt    5      64.010 ±    0.002    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                1.0      true         16    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                1.0      true         32    1024  thrpt    5      44.427 ±    0.511  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                1.0      true         32    1024  thrpt    5       1.802 ±    0.024  MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                1.0      true         32    1024  thrpt    5      64.009 ±    0.001    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                1.0      true         32    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                1.0     false          4    1024  thrpt    5      47.716 ±    0.682  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                1.0     false          4    1024  thrpt    5      ≈ 10⁻⁴             MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                1.0     false          4    1024  thrpt    5       0.009 ±    0.001    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                1.0     false          4    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                1.0     false          8    1024  thrpt    5      50.257 ±    0.772  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                1.0     false          8    1024  thrpt    5      ≈ 10⁻⁴             MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                1.0     false          8    1024  thrpt    5       0.008 ±    0.001    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                1.0     false          8    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                1.0     false         16    1024  thrpt    5      46.094 ±    1.154  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                1.0     false         16    1024  thrpt    5      ≈ 10⁻⁴             MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                1.0     false         16    1024  thrpt    5       0.009 ±    0.001    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                1.0     false         16    1024  thrpt    5         ≈ 0             counts
MessagePackingBenchmark.utf8Packer                                                    1                1.0     false         32    1024  thrpt    5      45.999 ±    0.803  ops/ms
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate                                     1                1.0     false         32    1024  thrpt    5      ≈ 10⁻⁴             MB/sec
MessagePackingBenchmark.utf8Packer:·gc.alloc.rate.norm                                1                1.0     false         32    1024  thrpt    5       0.009 ±    0.001    B/op
MessagePackingBenchmark.utf8Packer:·gc.count                                          1                1.0     false         32    1024  thrpt    5         ≈ 0             counts


     */

    @State(Scope.Benchmark)
    public static class DataState {

        @Param({"1024"})
        int size;

        @Param({"4", "8", "16", "32"})
        int keySize;


        long[] longs;
        String[] strings;
        byte[][] utf8s;

        public void init() {
            longs = new long[size];
            strings = new String[size];
            utf8s = new byte[size][];
            for (int i = 0; i < size; ++i) {
                longs[i] = ThreadLocalRandom.current().nextLong(-Long.MIN_VALUE, Long.MAX_VALUE);
                strings[i] = Strings.create(keySize);
                utf8s[i] = strings[i].getBytes(UTF_8);
            }
        }
    }

    @State(Scope.Benchmark)
    public static class PackerState extends DataState {

        @Param({"1"})
        int bufferSizeMB;

        @Param({"true", "false"})
        boolean direct;

        @Param({"0.0", "0.1", "0.2", "1.0"})
        double cachePercentage;

        ByteBuffer buffer;
        Packer packer;
        Map<CharSequence, byte[]> cache;
        Function<CharSequence, byte[]> toBytes;

        @Setup(Level.Trial)
        public void init() {
            super.init();
            buffer = direct
                    ? ByteBuffer.allocateDirect(bufferSizeMB << 20)
                    : ByteBuffer.allocate(bufferSizeMB << 20);
            packer = new Packer(buff -> {}, buffer);
            cache = new HashMap<>();
            for (String s : strings) {
                if (ThreadLocalRandom.current().nextDouble() < cachePercentage) {
                    cache.put(s, s.getBytes(UTF_8));
                }
            }
            toBytes = cache::get;
        }
    }

    @State(Scope.Benchmark)
    public static class MessagePackerState extends DataState {

        @Param({"1"})
        int bufferSizeMB;

        ArrayBufferOutput messageBufferOutput;
        MessagePacker packer;

        @Setup(Level.Trial)
        public void init() {
            super.init();
            messageBufferOutput = new ArrayBufferOutput(bufferSizeMB << 20);
            packer = MessagePack.newDefaultPacker(messageBufferOutput);
        }
    }


    @Benchmark
    public void packer(PackerState state, Blackhole bh) {
        Packer packer = state.packer;
        Function<CharSequence, byte[]> toBytes = state.toBytes;
        for (int i = 0; i < state.size; ++i) {
            packer.writeMapHeader(1);
            packer.writeString(state.strings[i], toBytes);
            packer.writeLong(state.longs[i]);
        }
        packer.flush();
        bh.consume(state.buffer);
    }


    @Benchmark
    public void messagePacker(MessagePackerState state, Blackhole bh) throws IOException {
        MessagePacker packer = state.packer;
        ArrayBufferOutput output = state.messageBufferOutput;
        packer.packArrayHeader(state.size);
        for (int i = 0; i < state.size; ++i) {
            packer.packMapHeader(1);
            packer.packString(state.strings[i]);
            packer.packLong(state.longs[i]);
        }
        packer.flush();
        bh.consume(output);
        output.clear();
        packer.clear();
    }

    @Benchmark
    public void utf8Packer(PackerState state, Blackhole bh) {
        Packer packer = state.packer;
        Function<CharSequence, byte[]> toBytes = state.toBytes;
        for (int i = 0; i < state.size; ++i) {
            packer.writeMapHeader(1);
            byte[] utf8 = state.utf8s[i];
            packer.writeUTF8(utf8, 0, utf8.length);
            packer.writeLong(state.longs[i]);
        }
        packer.flush();
        bh.consume(state.buffer);
    }


    @Benchmark
    public void utf8MessagePacker(MessagePackerState state, Blackhole bh) throws IOException {
        MessagePacker packer = state.packer;
        ArrayBufferOutput output = state.messageBufferOutput;
        packer.packArrayHeader(state.size);
        for (int i = 0; i < state.size; ++i) {
            packer.packMapHeader(1);
            byte[] utf8 = state.utf8s[i];
            packer.packRawStringHeader(utf8.length);
            packer.writePayload(utf8);
            packer.packLong(state.longs[i]);
        }
        packer.flush();
        bh.consume(output);
        output.clear();
        packer.clear();
    }

}
