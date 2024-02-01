package com.lightstreamer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class BaseConsumer extends Thread {

    private String kafkabootstrapstring;

    private String kafkaconsumergroupid;

    private String ktopicname;

    private boolean goconsume;

    private static Logger logger = LogManager.getLogger("kafkademo-adapters");

    public BaseConsumer(String kafka_bootstrap_string, String kgroupid, String topicname) {
        this.kafkabootstrapstring = kafka_bootstrap_string;
        this.kafkaconsumergroupid = kgroupid;
        this.ktopicname = topicname;
        this.goconsume = true;
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
            consumer.subscribe(Arrays.asList(ktopicname));
            while (goconsume) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(7000));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("Group id " + kafkaconsumergroupid + " offset = " + record.offset()
                            + ", message = " + record.value());

                    // To do.....

                }
                System.out.println("wait for new messages");
            }
            logger.info("End consumer loop");
        } catch (Exception e) {
            System.out.println("Error during consumer loop: " + e.getMessage());
        }

    }

}
