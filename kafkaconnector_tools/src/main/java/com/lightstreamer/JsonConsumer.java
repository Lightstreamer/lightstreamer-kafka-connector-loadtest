package com.lightstreamer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lightstreamer.internal.Set;

import java.util.Arrays;
import java.util.Properties;

public class JsonConsumer extends Thread {

    private String kafkabootstrapstring;

    private String kafkaconsumergroupid;

    private String ktopicname;

    private boolean goconsume;

    private boolean iamblackcanary;

    private static final Logger logger = LogManager.getLogger(JsonConsumer.class);

    private StatisticsManager stats;

    private int timediff(String timestampString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime timestamp = LocalDateTime.parse(timestampString, formatter);
        LocalDateTime oraAttuale = LocalDateTime.now();

        int differenzaMillisecondi = (int) Duration.between(timestamp, oraAttuale).toMillis();

        return differenzaMillisecondi;
    }

    public JsonConsumer(String kafka_bootstrap_string, String kgroupid, String topicname, boolean bc,
            StatisticsManager sts) {

        this.kafkabootstrapstring = kafka_bootstrap_string;
        this.kafkaconsumergroupid = kgroupid;
        this.ktopicname = topicname;
        this.goconsume = true;
        this.iamblackcanary = bc;
        this.stats = sts;

        logger.info("Consumer " + kgroupid + " initialized, I am black canarin: " + this.iamblackcanary);
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
        props.put("key.deserializer", org.apache.kafka.common.serialization.StringDeserializer.class);
        props.put("value.deserializer", io.confluent.kafka.serializers.KafkaJsonDeserializer.class);

        try {
            KafkaConsumer<String, TestObj> consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Arrays.asList(ktopicname));

            int k = -1;
            while (goconsume) {
                ConsumerRecords<String, TestObj> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, TestObj> record : records) {
                    TestObj message = record.value();

                    logger.info("Timestamp: " + message.timestamp + ", sndValue: " + message.sndValue);
                    // if (iamblackcanary) {
                    // String tsmsg = message.getTimestamp();
                    // logger.info("Timestamp: " + tsmsg);
                    // int diff = timediff(tsmsg);

                    // stats.onData(diff);

                    // if (k == 0) {

                    // logger.info("Group id " + kafkaconsumergroupid + " offset = " +
                    // record.offset()
                    // + ", message = " + message.getFstValue());

                    // logger.debug("------------------- " + diff);
                    // }
                    // if (++k == 1000)
                    // k = 0;
                    // }

                    logger.debug("Group id " + kafkaconsumergroupid + " offset = " + record.offset());

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
