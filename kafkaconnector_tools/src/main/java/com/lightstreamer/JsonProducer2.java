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
import java.util.concurrent.atomic.AtomicLong;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonProducer2 extends BaseProducer {

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

    private static final Logger logger = LoggerFactory.getLogger(JsonProducer2.class);

    private static final Random random = new SecureRandom();

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
    private int producerId;

    public JsonProducer2(int producerId, AtomicLong globalMessageCount, String kafka_bootstrap_string, String pid,
            String topicname,
            int pause, int msgsize,
            boolean addPrefix, boolean useLargeStrings) {
        super(kafka_bootstrap_string, pid, topicname, pause, msgsize);
        this.producerId = producerId;
        this.globalMessageCount = globalMessageCount;
        this.addPrefix = addPrefix;
        this.useLargeStrings = useLargeStrings;
        logger.info("Json producer: " + pid + ", prefix: " + addPrefix + ", ok.");

        for (int i = 0; i < strings.length; i++) {
            largeStrings[i] = buildRepeatedString(strings[i], 500);
        }
    }

    @Override
    public void run() {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkabootstrapstring);
        props.put("linger.ms", 50);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                io.confluent.kafka.serializers.KafkaJsonSerializer.class);

        Producer<String, TestObj> producer = new KafkaProducer<>(props);
        publish(producer);
    }

    private void publish(Producer<String, TestObj> producer) {
        Instant starInstant = Instant.now();
        while (true) {
            String prefix = addPrefix ? "PREFIX-" : "";
            String[] keyArray = useLargeStrings ? largeStrings : strings;
            int index = random.nextInt(keyArray.length);
            String sndV = keyArray[index];

            TestObj message = new TestObj(prefix + generateMillisTS(), generateRandomString(512), sndV,
                    generateRndInt());
            logger.debug("ProducerId - {}, New message for :{}", producerId, message.sndValue);
            try {
                producer.send(new ProducerRecord<>(topicName, sndV, message),
                        (metadata, exception) -> {
                            if (exception != null) {
                                logger.error("Error while producing message to topic : " + metadata.topic(),
                                        exception);
                                return;
                            }

                            Instant now = Instant.now();
                            Duration elapsed = Duration.between(starInstant, now);
                            logger.info("ProducerId - {} - Sent {} in {} seconds", producerId,
                                    globalMessageCount.incrementAndGet(), elapsed.toSeconds());
                        });
            } catch (Exception e) {
                logger.error("Error during sending message : " + e.getMessage());
                throw new RuntimeException(e);
            }
        }

    }
}
