/*
 * Copyright (C) 2024 Lightstreamer Srl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.lightstreamer;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.lightstreamer.proto.PriceInfo;

/**
 * JMH Benchmark comparing the payload generation performance between
 * SimpleProducer and ProtobufProducer makePayload methods.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class PayloadBenchmark {

    private SimpleProducer simpleProducer;
    private ProtobufProducer protobufProducer;
    private Random random;
    private String[] testKeys;
    
    @Setup(Level.Trial)
    public void setup() {
        // Initialize producers (we don't need actual Kafka connection for benchmarking)
        simpleProducer = new SimpleProducer("dummy", "test", "test");
        protobufProducer = new ProtobufProducer("dummy", "test", "test");
        
        // Create random instance for testing
        random = new Random(42); // Fixed seed for reproducible results
        
        // Pre-generate test keys for consistent testing
        testKeys = new String[100];
        for (int i = 0; i < testKeys.length; i++) {
            testKeys[i] = String.format("META250801P00680%03d", i);
        }
    }

    @Benchmark
    public byte[] benchmarkSimpleProducerPayload() {
        String key = testKeys[random.nextInt(testKeys.length)];
        return simpleProducer.makePayload(random, key);
    }

    @Benchmark
    public PriceInfo benchmarkProtobufProducerPayload() {
        String key = testKeys[random.nextInt(testKeys.length)];
        return protobufProducer.makePayload(random, key);
    }
    
    /**
     * Additional benchmark to test serialization performance
     */
    @Benchmark
    public byte[] benchmarkProtobufSerialization() {
        String key = testKeys[random.nextInt(testKeys.length)];
        PriceInfo priceInfo = protobufProducer.makePayload(random, key);
        
        // Serialize to byte array like Kafka would do
        return priceInfo.toByteArray();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(PayloadBenchmark.class.getSimpleName())
                .shouldFailOnError(true)
                .shouldDoGC(true)
                .jvmArgs("-Xmx2g", "-Xms2g")
                .build();

        new Runner(opt).run();
    }
}