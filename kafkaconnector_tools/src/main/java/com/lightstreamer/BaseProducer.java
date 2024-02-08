package com.lightstreamer;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.config.ConfigException;

public class BaseProducer extends Thread {

    private boolean goproduce;

    private String kafkabootstrapstring;

    private String producerid;

    private String ktopicname;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private static final int MSG_LEN = 1024;

    private static final int MSG_FREQ = 2000;

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

    public BaseProducer(String kafka_bootstrap_string, String pid, String topicname) {
        this.kafkabootstrapstring = kafka_bootstrap_string;
        this.producerid = pid;
        this.ktopicname = topicname;
        this.goproduce = true;
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
            RecordMetadata rmtdta;

            Producer<String, String> producer = new KafkaProducer<>(props);

            while (goproduce) {
                String message = generateMillisTS() + "-" + this.producerid + "-" + generateRandomString(MSG_LEN);

                System.out.println("New Message : " + message);

                futurek = producer
                        .send(new ProducerRecord<String, String>(ktopicname, message));

                rmtdta = futurek.get();

                System.out.println("Sent message to partition : " + rmtdta.partition());

                Thread.sleep(MSG_FREQ);
            }

            producer.close();

        } catch (Exception e) {
            System.out.println("Error during producer loop: " + e.getMessage());
        }
    }
}
