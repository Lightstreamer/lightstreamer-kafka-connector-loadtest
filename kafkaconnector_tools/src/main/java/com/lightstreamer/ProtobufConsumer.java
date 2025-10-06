/*
 * Copyright (C) 2024 Lightstreamer Srl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.lightstreamer;

import java.io.FileInputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.HdrHistogram.Histogram;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lightstreamer.proto.PriceInfo;

public class ProtobufConsumer extends BaseConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ProtobufConsumer.class);

    private int intervalMessageCounter;

    private Histogram histogram;

    public ProtobufConsumer(String kafka_bootstrap_string, String kgroupid, String topicname, boolean bc,
            StatisticsManager sts) {

        super(kafka_bootstrap_string, kgroupid, topicname, bc, sts);

        logger.info("Simple consumer {} go!", kgroupid);
        this.histogram = new Histogram(3_600_000_000_000L, 3); // fino a 1h, 3 cifre
    }

    @Override
    public void run() {

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("consumer.properties")) {
            props.load(fis);
        } catch (Exception e) {
            logger.error("Error loading consumer properties file: " + e.getMessage());
            throw new RuntimeException(e);
        }
        props.setProperty("bootstrap.servers", kafkabootstrapstring);
        props.setProperty("group.id", kafkaconsumergroupid);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ProtoTestObjDeserializer.class);
        int logRecordCount = Integer.parseInt(props.getProperty("log.record.count", "1000"));

        try {
            KafkaConsumer<String, PriceInfo> consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Arrays.asList(ktopicname));

            logger.debug("Consumer {} subscribed to topic {}", kafkaconsumergroupid, ktopicname);

            while (true) {
                ConsumerRecords<String, PriceInfo> records = consumer.poll(Duration.ofMillis(Long.MAX_VALUE));
                logger.debug("polled {} messages.", records.count());
                long currentTimestamp = System.currentTimeMillis();
                for (ConsumerRecord<String, PriceInfo> record : records) {
                    PriceInfo message = record.value();

                    long receivedTimestamp = record.timestamp();
                    long latency = currentTimestamp - receivedTimestamp;
                    histogram.recordValue(latency);

                    intervalMessageCounter++;
                    if (intervalMessageCounter == logRecordCount) {
                        logger.debug("Offset = " + record.offset() + ", message = " + message);
                        printClientLatencyReport();
                        intervalMessageCounter = 0;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error during consumer loop: " + e.getMessage());
        }
    }

    private void printClientLatencyReport() {
        logger.info("---- ClientLatency report ----");
        logger.info("Number of samples: {}", histogram.getTotalCount());
        printLatency(histogram, 50);
        printLatency(histogram, 95);
        printLatency(histogram, 98);
        printLatency(histogram, 99);
        printLatencyMax(histogram);
    }

    private void printLatency(Histogram histogram, int percentile) {
        logger.info("Latency p{}: {} ms",
                percentile,
                String.format("%d", histogram.getValueAtPercentile(percentile)));
    }

    private void printLatencyMax(Histogram histogram) {
        logger.info("Latency max: {} ms\n",
                String.format("%d", histogram.getMaxValue()));
    }

    public static class ProtoTestObjDeserializer
            implements org.apache.kafka.common.serialization.Deserializer<com.lightstreamer.proto.PriceInfo> {

        @Override
        public PriceInfo deserialize(String topic, byte[] data) {
            try {
                return com.lightstreamer.proto.PriceInfo.parseFrom(data);
            } catch (Exception e) {
                logger.error("Error deserializing protobuf message: " + e.getMessage());
                return null;
            }
        }

    }

    public static void main(String[] args) {
        PriceInfo priceInfo = com.lightstreamer.proto.PriceInfo.newBuilder()
                .setSymbol("test")
                .setLS(0.1f)
                .setLSSize(10)
                .setBid(0.1f)
                .setBidSize(10)
                .setAsk(0.1f)
                .setAskSize(10)
                .setCurrTime("1")
                .setHigh(0.1f)
                .setLow(0.1f)
                .setVol(10)
                .build();
        byte[] serialized = priceInfo.toByteArray();
        ProtoTestObjDeserializer deserializer = new ProtoTestObjDeserializer();
        PriceInfo deserialized = deserializer.deserialize("test-topic", serialized);
        System.out.println("Original: " + priceInfo);
    }

}
