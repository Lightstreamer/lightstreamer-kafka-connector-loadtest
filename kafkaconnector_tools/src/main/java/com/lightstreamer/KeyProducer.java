package com.lightstreamer;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Future;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KeyProducer extends Thread {

    private boolean goproduce;

    private String kafkabootstrapstring;

    private String producerid;

    private String ktopicname;

    private int millisp;

    private int msg_size;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    String[] stringkeys = { "Apple", "Banana", "Orange", "Grape", "Pineapple" };

    private static final Logger logger = LogManager.getLogger(KeyProducer.class);

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

    public KeyProducer(String kafka_bootstrap_string, String pid, String topicname, int pause, int msgsize) {
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
            Random random = new Random();

            Producer<String, String> producer = new KafkaProducer<>(props);

            while (goproduce) {
                String message = generateMillisTS() + "-" + this.producerid + "-" + generateRandomString(msg_size);

                int index = random.nextInt(stringkeys.length);
                String key = stringkeys[index];

                logger.debug("New Message : " + message + ", key: " + key);

                futurek = producer
                        .send(new ProducerRecord<String, String>(ktopicname, key, message));

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
