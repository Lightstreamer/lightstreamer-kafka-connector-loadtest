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
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JsonProducer extends BaseProducer {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    String[] strings = { "Apple", "Banana", "Orange", "Grape", "Pineapple",
            "Strawberry", "Watermelon", "Mango", "Kiwi", "Lemon",
            "Peach", "Cherry", "Blueberry", "Raspberry", "Blackberry",
            "Coconut", "Pomegranate", "Cantaloupe", "Apricot", "Fig",
            "Plum", "Pear", "Avocado", "Lychee", "Guava",
            "Dragonfruit", "Passionfruit", "Papaya", "Melon", "Lime",
            "Nectarine", "Persimmon", "Starfruit", "Tangerine", "Durian",
            "Kumquat", "Cranberry", "Rambutan", "Mangosteen", "Jackfruit" };

    private static final Logger logger = LogManager.getLogger(JsonProducer.class);

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

    public JsonProducer(String kafka_bootstrap_string, String pid, String topicname, int pause, int msgsize) {
        super(kafka_bootstrap_string, pid, topicname, pause, msgsize);

        logger.info("Json producer " + pid + " ok.");
    }

    @Override
    public void run() {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkabootstrapstring);
        props.put("linger.ms", 1);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                io.confluent.kafka.serializers.KafkaJsonSerializer.class);

        try {
            Future<RecordMetadata> futurek;

            Producer<String, TestObj> producer = new KafkaProducer<>(props);

            while (goproduce) {
                int index = random.nextInt(strings.length);
                String sndV = strings[index];

                TestObj message = new TestObj(generateMillisTS(), generateRandomString(512), sndV, generateRndInt());

                logger.debug("New Message : " + message.sndValue);

                futurek = producer
                        .send(new ProducerRecord<String, TestObj>(ktopicname, sndV, message));

                logger.debug("Sent message : " + futurek.isDone());

                /*
                 * RecordMetadata rmtdta = futurek.get();
                 * 
                 * logger.debug("Partition : " + rmtdta.partition() + ", " + rmtdta.offset());
                 */

                Thread.sleep(millisp);
            }

            producer.close();

        } catch (Exception e) {
            logger.error("Error during producer loop: " + e.getMessage());
        }
    }

}
