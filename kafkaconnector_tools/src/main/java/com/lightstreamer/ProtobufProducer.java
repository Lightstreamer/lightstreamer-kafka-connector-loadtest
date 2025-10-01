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
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lightstreamer.proto.PriceInfo;

public class ProtobufProducer extends BaseProducer {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    String[] strings = {
            "META250801P00680000",
            "META250801P00680001",
            "META250801P00680002",
            "META250801P00680003",
            "META250801P00680004",
            "META250801P00680005",
            "META250801P00680006",
            "META250801P00680007",
            "META250801P00680008",
            "META250801P00680009",
            "META250801P00680010",
            "META250801P00680011",
            "META250801P00680012",
            "META250801P00680013",
            "META250801P00680014",
            "META250801P00680015",
            "META250801P00680016",
            "META250801P00680017",
            "META250801P00680018",
            "META250801P00680019",
            "META250801P00680020",
            "META250801P00680021",
            "META250801P00680022",
            "META250801P00680023",
            "META250801P00680024",
            "META250801P00680025",
            "META250801P00680026",
            "META250801P00680027",
            "META250801P00680028",
            "META250801P00680029",
            "META250801P00680030",
            "META250801P00680031",
            "META250801P00680032",
            "META250801P00680033",
            "META250801P00680034",
            "META250801P00680035",
            "META250801P00680036",
            "META250801P00680037",
            "META250801P00680038",
            "META250801P00680039",
    };

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
        return Instant.now().toString();
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

    public ProtobufProducer(AtomicLong globalMessageCount, String kafka_bootstrap_string, String pid,
            String topicname,
            int pause, int msgsize,
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
        try (FileInputStream fis = new FileInputStream("publisher.properties")) {
            props.load(fis);
        } catch (Exception e) {
            logger.error("Error loading publisher properties file: " + e.getMessage());
            throw new RuntimeException(e);
        }
        props.put("bootstrap.servers", kafkabootstrapstring);
        props.put("linger.ms", 50);
        props.put("acks", "0");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                ProtoTestObjSerializer.class);

        Producer<String, com.lightstreamer.proto.PriceInfo> producer = new KafkaProducer<>(props);
        // publish(producer);
        publishMessages(producer, Integer.parseInt(props.getProperty("rate", "100000")));
    }

    // private void publish(Producer<String, com.lightstreamer.proto.TestObj> producer) {
    //     Instant starInstant = Instant.now();
    //     while (true) {
    //         String[] keyArray = useLargeStrings ? largeStrings : strings;
    //         int index = random.nextInt(keyArray.length);
    //         String sndV = keyArray[index];

    //         com.lightstreamer.proto.TestObj message = com.lightstreamer.proto.TestObj.newBuilder()
    //                 .setTimestamp(prefix + generateMillisTS())
    //                 .setFstValue(generateRandomString(512))
    //                 .setSndValue(sndV)
    //                 .setIntNum(generateRndInt())
    //                 .build();
    //         logger.debug("ProducerId - {}, New message for :{}", producerid, message.toString());
    //         try {
    //             producer.send(new ProducerRecord<>(ktopicname, sndV, message),
    //                     (metadata, exception) -> {
    //                         if (exception != null) {
    //                             logger.error("Error while producing message to topic : " + metadata.topic(),
    //                                     exception);
    //                             return;
    //                         }

    //                         Instant now = Instant.now();
    //                         Duration elapsed = Duration.between(starInstant, now);
    //                         logger.info("ProducerId - {} - Sent {} in {} seconds", producerid,
    //                                 globalMessageCount.incrementAndGet(), elapsed.toSeconds());
    //                     });
    //         } catch (Exception e) {
    //             logger.error("Error during sending message : " + e.getMessage());
    //             throw new RuntimeException(e);
    //         }
    //     }

    // }

    public void publishMessages(Producer<String, PriceInfo> producer, int targetRate) {
        long nanosPerMessage = 1_000_000_000L / targetRate;

        long nextSendTime = System.nanoTime();
        long start = System.nanoTime();
        String[] keyArray = useLargeStrings ? largeStrings : strings;
        long sentMessages = 0;
        Random rnd = new Random();
        while (true) {
            int index = rnd.nextInt(keyArray.length);
            String key = keyArray[index];

            com.lightstreamer.proto.PriceInfo payload = com.lightstreamer.proto.PriceInfo.newBuilder()
                    .setSymbol(key)
                    .setHigh(rnd.nextFloat() * 100.0f)
                    .setLS(rnd.nextFloat() * 100.0f)
                    .setLow(rnd.nextFloat() * 100.0f)
                    .setAsk(rnd.nextFloat() * 100.0f)
                    .setBid(rnd.nextFloat() * 100.0f)
                    .setCurrTime(String.valueOf(System.nanoTime()))
                    .build();
            producer.send(new ProducerRecord<>(ktopicname, key, payload));
            sentMessages++;

            // calcola quando dovrebbe partire il prossimo
            nextSendTime += nanosPerMessage;
            long sleepTime = nextSendTime - System.nanoTime();
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime / 1_000_000, (int) (sleepTime % 1_000_000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // log ogni 5 secondi
            if (sentMessages % (targetRate * 5) == 0) {
                long now = System.nanoTime();
                double elapsedSec = (now - start) / 1e9;
                double achievedRate = sentMessages / elapsedSec;
                System.out.printf("Sent %,d messages in %.2f s (target=%d msg/s, got=%.2f msg/s)%n",
                        sentMessages, elapsedSec, targetRate, achievedRate);
            }
        }

        // long endTime = System.nanoTime();
        // double seconds = (endTime - startTime) / 1e9;
        // System.out.printf("Published %d messages in %.2f s (%.2f msg/s)%n",
        // targetRate, seconds, targetRate / seconds);

        // producer.close();
    }

    public static class ProtoTestObjSerializer implements Serializer<com.lightstreamer.proto.PriceInfo> {

        @Override
        public byte[] serialize(String topic, com.lightstreamer.proto.PriceInfo data) {
            if (data == null) {
                return null;
            }
            return data.toByteArray();
        }
    }
}
