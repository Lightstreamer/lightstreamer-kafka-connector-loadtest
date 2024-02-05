package com.lightstreamer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private boolean iamblackcanary;

    private StatisticsCalculator stats;

    private static Logger logger = LogManager.getLogger("kafkademo-adapters");

    private long timediff(String timestampString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime timestamp = LocalDateTime.parse(timestampString, formatter);
        LocalDateTime oraAttuale = LocalDateTime.now();

        long differenzaMillisecondi = Duration.between(timestamp, oraAttuale).toMillis();

        return differenzaMillisecondi;
    }

    public BaseConsumer(String kafka_bootstrap_string, String kgroupid, String topicname, boolean bc) {
        int lenstats = 0;

        this.kafkabootstrapstring = kafka_bootstrap_string;
        this.kafkaconsumergroupid = kgroupid;
        this.ktopicname = topicname;
        this.goconsume = true;
        this.iamblackcanary = bc;
        if (bc)
            lenstats = 10000;
        this.stats = new StatisticsCalculator(lenstats);
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
            int k = 0;
            while (goconsume) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(7000));
                for (ConsumerRecord<String, String> record : records) {
                    if (iamblackcanary) {
                        String message = record.value();
                        String tsmsg = message.substring(0, 23);
                        long diff = timediff(tsmsg);
                        stats.addValue(diff);
                        System.out.println("------------------- " + diff);
                        if (k == 0) {
                            System.out.println("Group id " + kafkaconsumergroupid + " offset = " + record.offset()
                                    + ", message = " + message);

                            System.out
                                    .println("Mean: " + stats.calculateMean() + ", Median = " + stats.calculateMedian()
                                            + ", confidence = " + stats.calculateConfidenceInterval(500));
                        }
                        if (++k == 10)
                            k = 0;
                    }

                    // To do.....

                }
                // System.out.println("wait for new messages");
            }
            logger.info("End consumer loop");
        } catch (Exception e) {
            System.out.println("Error during consumer loop: " + e.getMessage());
        }

    }

}
