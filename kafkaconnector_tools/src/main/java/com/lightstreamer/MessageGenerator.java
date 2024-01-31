package com.lightstreamer;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Future;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageGenerator {

    private static Logger logger = LogManager.getLogger("kafkademo-producer");

    private static String kconnstring;

    private static boolean go = true;

    private static String topicname;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private static final int MSG_LEN = 1024;

    private static final int MSG_FREQ = 100;

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

    private static void kafkaproducerloop() {
        Properties props = new Properties();
        props.put("bootstrap.servers", kconnstring);
        props.put("linger.ms", 1);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        try {
            Future<RecordMetadata> futurek;
            RecordMetadata rmtdta;

            Producer<String, String> producer = new KafkaProducer<>(props);

            while (go) {
                String message = generateRandomString(MSG_LEN);

                logger.info("New Message : " + message);

                futurek = producer
                        .send(new ProducerRecord<String, String>(topicname, generateMillisTS() + "-" + message));

                rmtdta = futurek.get();

                logger.info("Sent message to" + rmtdta.partition());

                Thread.sleep(MSG_FREQ);
            }

            producer.close();

        } catch (Exception e) {
            logger.error("Error during producer loop: " + e.getMessage());
        }
    }

    public static void main(String[] args) {

        logger.info("Start Kafka demo producer: " + args.length);

        if (args.length < 2) {
            logger.error("Missing arguments bootstrap-servers topioc-name");
            return;
        }

        for (int i = 0; i < 100; i++) {
            String randomString = generateRandomString(1024);
            System.out.println(generateMillisTS() + "-" + randomString);
        }

        kconnstring = args[0];
        topicname = args[1];

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                kafkaproducerloop();
            }
        });
        t1.start();

        String input = System.console().readLine();
        while (!input.equalsIgnoreCase("stop")) {
            input = System.console().readLine();
        }

        go = false;

        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            // ...
        }

        logger.info("End Kafka demo producer.");
    }
}
