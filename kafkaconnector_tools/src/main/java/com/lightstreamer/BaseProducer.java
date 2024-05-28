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
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BaseProducer extends Thread {

    protected boolean goproduce;

    protected String kafkabootstrapstring;

    protected String producerid;

    protected String ktopicname;

    protected int millisp;

    protected int msg_size;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private static final Logger logger = LogManager.getLogger(BaseProducer.class);

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

    private static String generateMillisTS() {
        long milliseconds = System.currentTimeMillis();

        Date date = new Date(milliseconds);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        String formattedDate = sdf.format(date);

        return formattedDate;
    }

    public BaseProducer(String kafka_bootstrap_string, String pid, String topicname, int pause, int msgsize) {
        this.kafkabootstrapstring = kafka_bootstrap_string;
        this.producerid = pid;
        this.ktopicname = topicname;
        this.goproduce = true;
        this.millisp = pause;
        this.msg_size = msgsize;
    }

    public void stopproducing() {
        this.goproduce = false;
    }

    @Override
    public void run() {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkabootstrapstring);
        props.put("linger.ms", 1);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        try {
            Future<RecordMetadata> futurek;
            RecordMetadata rmtdta;

            Producer<String, String> producer = new KafkaProducer<>(props);

            while (goproduce) {
                String message = generateMillisTS() + "-" + this.producerid + "-" + generateRandomString(msg_size);

                logger.debug("New Message : " + message);

                futurek = producer
                        .send(new ProducerRecord<String, String>(ktopicname, message));

                // rmtdta = futurek.get();

                logger.debug("Sent message : " + futurek.isDone());

                Thread.sleep(millisp);
            }

            producer.close();

        } catch (Exception e) {
            logger.error("Error during producer loop: " + e.getMessage());
        }
    }
}
