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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class SimpleProducer extends RateLimitedKafkaProducer<byte[]> {

    private static final int PAYLOAD_SIZE = 66;
    private static final byte[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
            .getBytes();

    public SimpleProducer(String kafka_bootstrap_string, String pid,
            String topicName) {
        super(kafka_bootstrap_string, pid, topicName,
                org.apache.kafka.common.serialization.ByteArraySerializer.class);
    }

    @Override
    byte[] makePayload(Random rnd, String key) {
        byte[] messageBuffer = new byte[PAYLOAD_SIZE];  // Create new array each time
        for (int i = 0; i < PAYLOAD_SIZE; i++) {
            messageBuffer[i] = ALPHABET[ThreadLocalRandom.current().nextInt(ALPHABET.length)];
        }
        return messageBuffer;
    }

    public static void main(String[] args) {
        SimpleProducer p = new SimpleProducer("ec2-18-201-235-33.eu-west-1.compute.amazonaws.com:9092", "pid-0",
                "LTest");
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.submit(p);
    }

}
