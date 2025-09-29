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
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.HdrHistogram.Histogram;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleConsumer extends BaseConsumer {

    private static final Logger logger = LoggerFactory.getLogger(SimpleConsumer.class);

    public SimpleConsumer(String kafka_bootstrap_string, String kgroupid, String topicname, boolean bc,
            StatisticsManager sts) {

        super(kafka_bootstrap_string, kgroupid, topicname, bc, sts);

        logger.info("Simple consumer {} go!", kgroupid);
    }

    // @Override
    // protected int timediff(String timestampString) {
    //     Instant current = Instant.now();
    //     Instant received = Instant.parse(timestampString);

    //     logger.debug("Received timestamp: {}, Current timestamp {}", received, current);

    //     return (int) Duration.between(received, current).toMillis();
    // }



    @Override
    public void run() {
        Histogram histogram = new Histogram(3_600_000_000_000L, 3); // fino a 1h, 3 cifre
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("consumer.properties")) {
            props.load(fis);
        } catch (Exception e) {
            logger.error("Error loading consumer properties file: " + e.getMessage());
            throw new RuntimeException(e);
        }
        props.setProperty("bootstrap.servers", kafkabootstrapstring);
        props.setProperty("group.id", kafkaconsumergroupid);
        
        props.put("key.deserializer", org.apache.kafka.common.serialization.StringDeserializer.class);
        props.put("value.deserializer", org.apache.kafka.common.serialization.StringDeserializer.class);
        int logRecordCount = Integer.parseInt(props.getProperty("log.record.count", "1000"));

        try {
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Arrays.asList(ktopicname));

            logger.debug("Consumer {} subscribed to topic {}", kafkaconsumergroupid, ktopicname);

            int k = 0;
            int kk = 0;
            while (goconsume) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(Long.MAX_VALUE));

                logger.debug("polled {} messages.", records.count());
                for (ConsumerRecord<String, String> record : records) {
                    String message = record.value();

                    // logger.debug("Message: {}", message);
                    if (iamblackcanary) {
                        String tsmsg = message;
                        // int diff = timediff(tsmsg);
                        // int diff = (int)((System.nanoTime() - Long.parseLong(tsmsg)) / 1e6);
                        long latency = System.nanoTime() - Long.parseLong(tsmsg);

                        // stats.onData((int)(latency / 1_000_000.0));
                        // histogram.recordValue(latency);

                        k++;
                        if (k == logRecordCount) {
                            kk += k;
                            logger.debug("Offset = " + record.offset() + ", message = " + message);
                            logger.info("Received {} messages", kk);
                            // stats.generateReport();
                            // System.out.printf("Latency p50: %.3f ms%n", histogram.getValueAtPercentile(50) / 1_000_000.0);
                            // System.out.printf("Latency p95: %.3f ms%n", histogram.getValueAtPercentile(95) / 1_000_000.0);
                            // System.out.printf("Latency p98: %.3f ms%n", histogram.getValueAtPercentile(98) / 1_000_000.0);
                            // System.out.printf("Latency p99: %.3f ms%n", histogram.getValueAtPercentile(99) / 1_000_000.0);
                            // System.out.printf("Latency max: %.3f ms%n", histogram.getMaxValue() / 1_000_000.0);
                            k = 0;
                            
                        }
                    } else {
                        msg_counter++;
                        if ((msg_counter % 50000) == 0) {
                            logger.info("N. message received: " + msg_counter);
                        }
                    }
                }
                logger.trace("wait for new messages");
            }
            logger.info("End consumer loop");
        } catch (Exception e) {
            logger.error("Error during consumer loop: " + e.getMessage());
            e.printStackTrace();
        }

    }

}
