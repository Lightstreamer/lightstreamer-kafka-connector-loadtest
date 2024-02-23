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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KeyProducer extends BaseProducer {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    String[] strings = { "Apple", "Banana", "Orange", "Grape", "Pineapple",
            "Strawberry", "Watermelon", "Mango", "Kiwi", "Lemon",
            "Peach", "Cherry", "Blueberry", "Raspberry", "Blackberry",
            "Coconut", "Pomegranate", "Cantaloupe", "Apricot", "Fig",
            "Plum", "Pear", "Avocado", "Lychee", "Guava",
            "Dragonfruit", "Passionfruit", "Papaya", "Melon", "Lime",
            "Nectarine", "Persimmon", "Starfruit", "Tangerine", "Durian",
            "Kumquat", "Cranberry", "Rambutan", "Mangosteen", "Jackfruit" };

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
        super(kafka_bootstrap_string, pid, topicname, pause, msgsize);
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

                int index = random.nextInt(strings.length);
                String key = strings[index];

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
