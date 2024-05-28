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

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StandaloneJsonConsumer extends BaseConsumer {

    private static final Logger logger = LogManager.getLogger(StandaloneConsumer.class);

    public StandaloneJsonConsumer(String kafka_bootstrap_string, String topicname, boolean bc, StatisticsManager sts) {

        super(kafka_bootstrap_string, "", topicname, bc, sts);

        logger.info("Standalone Json go!");
    }

    @Override
    public void run() {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", kafkabootstrapstring);
        props.setProperty("group.id", kafkaconsumergroupid);
        props.setProperty("enable.auto.commit", "true");
        props.setProperty("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", org.apache.kafka.common.serialization.StringDeserializer.class);
        props.put("value.deserializer", io.confluent.kafka.serializers.KafkaJsonDeserializer.class);

        try {
            KafkaConsumer<String, LinkedHashMap> consumer = new KafkaConsumer<>(props);
            List<PartitionInfo> partitionInfos = null;
            partitionInfos = consumer.partitionsFor(ktopicname);

            if (partitionInfos != null) {
                List<TopicPartition> partitions = new ArrayList<TopicPartition>();
                for (PartitionInfo partition : partitionInfos) {
                    partitions.add(new TopicPartition(partition.topic(), partition.partition()));
                }

                consumer.assign(partitions);

                int k = -1;
                while (goconsume) {
                    ConsumerRecords<String, LinkedHashMap> records = consumer.poll(Duration.ofMillis(500));
                    for (ConsumerRecord<String, LinkedHashMap> record : records) {
                        LinkedHashMap<String, Object> message = record.value();

                        if (iamblackcanary) {
                            String tsmsg = message.get("timestamp").toString();

                            int diff = timediff(tsmsg);
                            stats.onData(diff);

                            if (k == 0) {
                                logger.info("Timestamp of the message {}.", tsmsg);
                                logger.info("\t\tlatency in ms " + diff);
                            }
                            if (++k == 1000)
                                k = 0;
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
            }
        } catch (Exception e) {
            logger.error("Error during consumer loop: " + e.getMessage());
        }

    }
}
