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

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtobufProducer extends BaseProducer {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    String[] strings = { "Apple", "Banana", "Orange", "Grape", "Pineapple",
            "Strawberry", "Watermelon", "Mango", "Kiwi", "Lemon",
            "Peach", "Cherry", "Blueberry", "Raspberry", "Blackberry",
            "Coconut", "Pomegranate", "Cantaloupe", "Apricot", "Fig",
            "Plum", "Pear", "Avocado", "Lychee", "Guava",
            "Dragonfruit", "Passionfruit", "Papaya", "Melon", "Lime",
            "Nectarine", "Persimmon", "Starfruit", "Tangerine", "Durian",
            "Kumquat", "Cranberry", "Rambutan", "Mangosteen", "Jackfruit" };

    private String[] largeStrings = new String[strings.length];

    private static final Logger logger = LoggerFactory.getLogger(ProtobufProducer.class);

    private static final Random random = new SecureRandom();

    private static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(randomIndex);
            sb.append(randomChar);
        }

        return sb.toString();
    }

    private int generateRndInt() {
        return random.nextInt();
    }

    private static String generateMillisTS() {
        long milliseconds = System.currentTimeMillis();

        Date date = new Date(milliseconds);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        String formattedDate = sdf.format(date);

        return formattedDate;
    }

    private static String buildRepeatedString(String base, int totalLength) {
        StringBuilder sb = new StringBuilder();
        while (sb.length() < totalLength) {
            sb.append(base);
        }
        return sb.length() > totalLength
                ? sb.substring(0, totalLength)
                : sb.toString();
    }

    private boolean addPrefix;
    private boolean useLargeStrings;
    private AtomicLong globalMessageCount;

    public ProtobufProducer(AtomicLong globalMessageCount, String kafka_bootstrap_string, String pid, String topicname, int pause, int msgsize,
            boolean addPrefix, boolean useLargeStrings) {
        super(kafka_bootstrap_string, pid, topicname, pause, msgsize);
        this.globalMessageCount = globalMessageCount;
        this.addPrefix = addPrefix;
        this.useLargeStrings = useLargeStrings;
        logger.info("Protobuf producer: " + pid + ", prefix: " + addPrefix + ", ok.");

        for (int i = 0; i < strings.length; i++) {
            largeStrings[i] = buildRepeatedString(strings[i], 500);
        }
    }

    @Override
    public void run() {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkabootstrapstring);
        props.put("linger.ms", 1);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                ProtoTestObjSerializer.class);

        try {
            Producer<String, com.lightstreamer.proto.TestObj> producer = new KafkaProducer<>(props);

            // final AtomicLong startTime = new AtomicLong(System.currentTimeMillis());
            final Instant starInstant = Instant.now();
            // final AtomicInteger messageCount = new AtomicInteger(0);

            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(() -> {
                if (!goproduce) {
                    executor.shutdown();
                    producer.close();
                    return;
                }
                String prefix = addPrefix ? "PREFIX-" : "";
                String[] keyArray = useLargeStrings ? largeStrings : strings;
                int index = random.nextInt(keyArray.length);
                String sndV = keyArray[index];

                com.lightstreamer.proto.TestObj message = com.lightstreamer.proto.TestObj.newBuilder()
                        .setTimestamp(prefix + generateMillisTS())
                        .setFstValue(generateRandomString(512))
                        .setSndValue(sndV)
                        .setIntNum(generateRndInt())
                        .build();
                logger.debug("New message for : " + message.getSndValue());

                try {
                    producer.send(new ProducerRecord<>(ktopicname, sndV, message),
                            (metadata, exception) -> {
                                if (exception != null) {
                                    logger.error("Error while producing message to topic : " + metadata.topic(),
                                            exception);
                                    return;
                                }

                                Instant now = Instant.now();

                                // long currentTime = System.currentTimeMillis();
                                // if (currentTime - startTime.get() >= 1000) {
                                //     logger.info("Messages sent in the last second: {}",  messageCount);
                                //     messageCount.set(0);
                                //     startTime.set(currentTime);
                                // } else {
                                //     messageCount.incrementAndGet();
                                // }
                                Duration elapsed = Duration.between(starInstant, now);
                                logger.info("Sent {} in {} seconds", globalMessageCount.incrementAndGet(), elapsed.toSeconds());
                            });
                } catch (Exception e) {
                    logger.error("Error during sending message : " + e.getMessage());
                }

            }, 0, millisp, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.error("Error during producer loop", e);
        }
    }

    public static class ProtoTestObjSerializer implements Serializer<com.lightstreamer.proto.TestObj> {

        @Override
        public byte[] serialize(String topic, com.lightstreamer.proto.TestObj data) {
            if (data == null) {
                return null;
            }
            return data.toByteArray();
        }
    }
}
