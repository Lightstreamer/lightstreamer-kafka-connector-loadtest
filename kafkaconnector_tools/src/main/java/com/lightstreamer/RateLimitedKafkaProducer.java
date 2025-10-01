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

import java.io.FileInputStream;
import java.util.Properties;
import java.util.Random;
import java.util.stream.IntStream;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RateLimitedKafkaProducer<T> extends BaseProducer {

    static final Logger logger = LoggerFactory.getLogger(RateLimitedKafkaProducer.class);

    private final Class<?> valueSerializeClass;
    private final String[] keys;

    public RateLimitedKafkaProducer(String kafka_bootstrap_string, String pid,
            String topicName, Class<?> valueSerializeClass) {
        super(kafka_bootstrap_string, pid, topicName, 0, 0);
        this.valueSerializeClass = valueSerializeClass;
        this.keys = IntStream.range(0, 40)
                .mapToObj(i -> String.format("META250801P00680%03d", i))
                .toArray(String[]::new);
        logger.info("Producer {}", pid);
    }

    @Override
    public final void run() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("publisher.properties")) {
            props.load(fis);
        } catch (Exception e) {
            logger.error("Error loading publisher properties file: " + e.getMessage());
            throw new RuntimeException(e);
        }
        props.put("bootstrap.servers", kafkabootstrapstring);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                valueSerializeClass);

        Producer<String, T> producer = new KafkaProducer<>(props);
        publishMessages(producer, Integer.parseInt(props.getProperty("rate", "100000")));
    }

    abstract T makePayload(Random rnd, String key);

    final protected void publishMessages(Producer<String, T> producer, int targetRate) {
        long nanosPerMessage = 1_000_000_000L / targetRate;
        long nextSendTime = System.nanoTime();
        long start = System.nanoTime();
        long sentMessages = 0;
        Random rnd = new Random();
        while (true) {
            // Select a random key from the predefined list
            String key = keys[rnd.nextInt(keys.length)];

            // Build and send the message
            producer.send(new ProducerRecord<>(topicName, key, makePayload(rnd, key)));
            sentMessages++;

            // Determines when the next message should be sent
            nextSendTime += nanosPerMessage;
            long sleepTime = nextSendTime - System.nanoTime();
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime / 1_000_000, (int) (sleepTime % 1_000_000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Every 5 seconds, print the achieved rate
            if (sentMessages % (targetRate * 5) == 0) {
                long now = System.nanoTime();
                double elapsedSec = (now - start) / 1e9;
                double achievedRate = sentMessages / elapsedSec;
                System.out.printf("Sent %,d messages in %.2f s (target=%d msg/s, got=%.2f msg/s)%n",
                        sentMessages, elapsedSec, targetRate, achievedRate);
            }
        }
    }

}
