package com.lightstreamer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BaseConsumer extends Thread {

    protected String kafkabootstrapstring;

    protected String kafkaconsumergroupid;

    protected String ktopicname;

    protected boolean goconsume;

    protected boolean iamblackcanary;

    private static final Logger logger = LogManager.getLogger(BaseConsumer.class);

    protected StatisticsManager stats;

    protected static long msg_counter = 0;

    protected int timediff(String timestampString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime timestamp = LocalDateTime.parse(timestampString, formatter);
        LocalDateTime oraAttuale = LocalDateTime.now();

        int diffmillis = (int) Duration.between(timestamp, oraAttuale).toMillis();

        return diffmillis;
    }

    public BaseConsumer(String kafka_bootstrap_string, String kgroupid, String topicname, boolean bc,
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
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        try {
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Arrays.asList(ktopicname));

            int k = -1;
            while (goconsume) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> record : records) {
                    String message = record.value();
                    String key = record.key();

                    if (iamblackcanary) {
                        String tsmsg = message.substring(0, 23);
                        int diff = timediff(tsmsg);

                        stats.onData(diff);

                        if (k == 0) {

                            logger.info("Group id " + kafkaconsumergroupid + " offset = " + record.offset()
                                    + ", message = " + message);

                            logger.debug("------------------- " + diff);
                        }
                        if (++k == 500)
                            k = 0;
                    } else {
                        msg_counter++;
                        if ((msg_counter % 50000) == 0) {
                            logger.info("N. message received: " + msg_counter);
                        }
                    }

                    logger.debug("Group id " + kafkaconsumergroupid + " offset = " + record.offset()
                            + ", key = " + key + ", message = " + message);

                }
                logger.trace("wait for new messages");
            }
            logger.info("End consumer loop");
        } catch (Exception e) {
            logger.error("Error during consumer loop: " + e.getMessage());
        }

    }

}
