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

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SimpleProducer extends RateLimitedKafkaProducer<ByteBuffer> {

    private static final byte[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
            .getBytes();

    private ByteBuffer messageBuffer = ByteBuffer.allocateDirect(256);

    public SimpleProducer(String kafka_bootstrap_string, String pid,
            String topicName) {
        super(kafka_bootstrap_string, pid, topicName,
                org.apache.kafka.common.serialization.ByteBufferDeserializer.class);
    }

    @Override
    ByteBuffer makePayload(Random rnd, String key) {
        return randomString(256);
    }

    public ByteBuffer randomString(int length) {
        for (int i = 0; i < length; i++) {
            messageBuffer.put(ALPHABET[ThreadLocalRandom.current().nextInt(ALPHABET.length)]);
        }
        messageBuffer.flip();
        return messageBuffer;
    }

}
