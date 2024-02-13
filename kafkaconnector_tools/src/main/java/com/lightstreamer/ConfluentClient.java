package com.lightstreamer;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ConfluentClient {

    private static Properties props;

    public static Properties loadConfig(final String configFile) throws IOException {
        if (!Files.exists(Paths.get(configFile))) {
            throw new IOException(configFile + " not found.");
        }
        final Properties cfg = new Properties();
        try (InputStream inputStream = new FileInputStream(configFile)) {
            cfg.load(inputStream);
        }
        return cfg;
    }

    public static void readdata() throws IOException {

        props.put(ConsumerConfig.GROUP_ID_CONFIG, "kafka-java-getting-started");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList("topic_0"));

        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(7000));
        for (ConsumerRecord<String, String> record : records) {
            System.out.printf("key = %s, value = %s%n", record.key(), record.value());
        }

    }

    public static void writedata() {
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        Producer<String, String> producer = new KafkaProducer<>(props);
        producer.send(new ProducerRecord<>("topic_0", "key-00", "Ciao da Giuseppe"));
        producer.close();
        System.out.printf("data produced.%n");
    }

    public static void main(String[] args) {
        try {
            props = loadConfig("client.properties");
            writedata();
            Thread.sleep(2500);
            readdata();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return;
    }

}
