package com.lightstreamer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StandaloneConsumer extends Thread {

    private String kafkabootstrapstring;

    private final String kafkaconsumergroupid = "";

    private String ktopicname;

    private boolean goconsume;

    private boolean iamblackcanary;

    private static final Logger logger = LogManager.getLogger(BaseConsumer.class);

    private StatisticsManager stats;

    private int timediff(String timestampString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime timestamp = LocalDateTime.parse(timestampString, formatter);
        LocalDateTime oraAttuale = LocalDateTime.now();

        int differenzaMillisecondi = (int) Duration.between(timestamp, oraAttuale).toMillis();

        return differenzaMillisecondi;
    }

    public StandaloneConsumer(String kafka_bootstrap_string, String topicname, boolean bc, StatisticsManager sts) {

        this.kafkabootstrapstring = kafka_bootstrap_string;
        this.ktopicname = topicname;
        this.goconsume = true;
        this.iamblackcanary = bc;
        this.stats = sts;

        logger.info("Standalone consumer initialized, I am black canarin: " + bc);
    }

    public void stopconsuming() {
        this.goconsume = false;
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
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(5000));
                    for (ConsumerRecord<String, String> record : records) {
                        if (iamblackcanary) {
                            String message = record.value();
                            String tsmsg = message.substring(0, 23);
                            int diff = timediff(tsmsg);

                            stats.onData(diff);

                            if (k == 0) {
                                logger.info("Group id " + kafkaconsumergroupid + " offset = " + record.offset()
                                        + ", message = " + message);

                                logger.debug("------------------- " + diff);
                            }
                            if (++k == 100)
                                k = 0;
                        }

                        // To do.....

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
