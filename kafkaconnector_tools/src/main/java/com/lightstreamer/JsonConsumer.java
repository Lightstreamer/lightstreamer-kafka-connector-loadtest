package com.lightstreamer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JsonConsumer extends BaseConsumer {

    private static final Logger logger = LogManager.getLogger(JsonConsumer.class);
    public JsonConsumer(String kafka_bootstrap_string, String kgroupid, String topicname, boolean bc,
            StatisticsManager sts) {

        super(kafka_bootstrap_string, kgroupid, topicname, bc, sts);

        logger.info("JSON consumer go!");
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

            logger.debug("consumer subscribed to topic " + ktopicname);

            int k = -1;
            while (goconsume) {
                ConsumerRecords<String, TestObj> records = consumer.poll(Duration.ofMillis(500));

                logger.debug("polled " + records.count() + " messages.");
                for (ConsumerRecord<String, TestObj> record : records) {
                    TestObj message = record.value();

                    logger.info("Timestamp: " + message.timestamp + ", sndValue: " + message.sndValue);
                    if (iamblackcanary) {
                        String tsmsg = message.timestamp;
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
        } catch (Exception e) {
            logger.error("Error during consumer loop: " + e.getMessage());
            e.printStackTrace();
        }

    }

}
