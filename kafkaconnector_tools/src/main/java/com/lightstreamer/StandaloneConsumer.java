package com.lightstreamer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StandaloneConsumer extends BaseConsumer {

    private static final Logger logger = LogManager.getLogger(StandaloneConsumer.class);

    public StandaloneConsumer(String kafka_bootstrap_string, String topicname, boolean bc, StatisticsManager sts) {

        super(kafka_bootstrap_string, "", topicname, bc, sts);

        logger.info("Standalone go!");
    }

    @Override
    public void run() {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", kafkabootstrapstring);
        props.setProperty("group.id", kafkaconsumergroupid);
        props.setProperty("enable.auto.commit", "true");
        props.setProperty("auto.commit.interval.ms", "1000");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        try {
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
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
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                    for (ConsumerRecord<String, String> record : records) {
                        String message = record.value();

                        if (iamblackcanary) {
                            String tsmsg = message.substring(0, 23);
                            int diff = timediff(tsmsg);

                            stats.onData(diff);

                            if (k == 0) {
                                logger.info("Offset = " + record.offset() + ", message = " + message);

                                logger.debug("------------------- " + diff);
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
