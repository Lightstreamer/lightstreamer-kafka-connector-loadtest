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

import java.util.stream.IntStream;

import org.HdrHistogram.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lightstreamer.client.ItemUpdate;
import com.lightstreamer.client.LightstreamerClient;
import com.lightstreamer.client.Subscription;
import com.lightstreamer.client.SubscriptionListener;

public class LightstreamerConsumer {

    private static final Logger logger = LoggerFactory.getLogger(LightstreamerConsumer.class);

    public static void main(String[] args) {
        if (args.length == 0) {
            logger.error("Server hostname is required as first argument");
            System.exit(1);
        }
        String serverAddress = args[0];
        int numberOfKeys = Integer.parseInt(args.length > 1 ? args[1] : "40");

        LightstreamerClient client = new LightstreamerClient(serverAddress, "KafkaConnector");
        client.addListener(new MyClientListener());
        client.connect();

        /**
         * Creates an array of 40 item names for testing purposes.
         * Each item follows the pattern "ltest-[key=META250801P00680XXX]" where XXX is
         * a
         * zero-padded 3-digit number starting from 000 to 039.
         * 
         * @return String array containing formatted item names for load testing
         */
        String[] items = IntStream.range(0, numberOfKeys)
                .mapToObj(i -> String.format("ltest-[key=META250801P00680%03d]", i))
                .toArray(String[]::new);
        String[] fields = { "volume", "high", "partition", "last", "offset", "low", "sym", "ask", "bid", "tradetime",
                "timestamp" };

        Subscription sub = new Subscription("DISTINCT", items, fields);
        sub.setDataAdapter("QuickStart");
        sub.setRequestedSnapshot("no");
        sub.addListener(new LatencyDumper());
        sub.setRequestedMaxFrequency("unfiltered");
        client.subscribe(sub);
    }

    private static class LatencyDumper implements SubscriptionListener {

        private static final int REPORT_INTERVAL_MESSAGE_COUNT = 10_000;

        private final Histogram histogram;
        private int intervalMessageCounter = 0;

        public LatencyDumper() {
            this.histogram = new Histogram(3_600_000_000_000L, 3); // up to 1h, 3 digits
        }

        @Override
        public void onClearSnapshot(String itemName, int itemPos) {
            logger.info("Server has cleared the current status of the chat");
        }

        @Override
        public void onCommandSecondLevelItemLostUpdates(int lostUpdates, String key) {
            // not on this subscription
        }

        @Override
        public void onCommandSecondLevelSubscriptionError(int code, String message, String key) {
            // not on this subscription
        }

        @Override
        public void onEndOfSnapshot(String arg0, int arg1) {
            logger.info("Snapshot is now fully received, from now on only real-time messages will be received");
        }

        @Override
        public void onItemLostUpdates(String itemName, int itemPos, int lostUpdates) {
            logger.info("{} lostUpdates messages were lost", lostUpdates);
        }

        @Override
        public void onItemUpdate(ItemUpdate update) {
            try {
                long currentTimestamp = System.currentTimeMillis();
                // long latency = System.nanoTime() - Long.parseLong(update.getValue("tradetime"));
                long receivedTimestamp = Long.parseLong(update.getValue("timestamp"));
                long latency = currentTimestamp - receivedTimestamp;
                histogram.recordValue(latency);

                intervalMessageCounter++;
                if (intervalMessageCounter == REPORT_INTERVAL_MESSAGE_COUNT) {
                    latencyReport();
                    intervalMessageCounter = 0;
                }
            } catch (Exception e) {
                logger.error("Error in onItemUpdate", e);
            }
        }

        private void latencyReport() {
            logger.info("---- Latency report ----");
            logger.info("Number of samples: {}", histogram.getTotalCount());
            printLatency(50);
            printLatency(95);
            printLatency(98);
            printLatency(99);
            printLatencyMax();
        }

        private void printLatency(int percentile) {
            logger.info("Latency p{}: {} ms",
                    percentile,
                    String.format("%d", histogram.getValueAtPercentile(percentile)));
        }

        private void printLatencyMax() {
            logger.info("Latency max: {} ms\n",
                    String.format("%d", histogram.getMaxValue()));
        }

        @Override
        public void onListenEnd() {
            logger.info("Stop listening to subscription events");
        }

        @Override
        public void onListenStart() {
            logger.info("Start listening to subscription events");
        }

        @Override
        public void onSubscription() {
            logger.info("Now subscribed to items, messages will now start coming in");
        }

        @Override
        public void onSubscriptionError(int code, String message) {
            logger.info("Cannot subscribe because of error " + code + ": " + message);
        }

        @Override
        public void onUnsubscription() {
            logger.info("Now unsubscribed from items, no more messages will be received");
        }

        @Override
        public void onRealMaxFrequency(String frequency) {
            logger.info("Frequency is " + frequency);
        }
    }

}
