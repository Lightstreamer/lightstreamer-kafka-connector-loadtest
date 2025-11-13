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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.HdrHistogram.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.lightstreamer.client.ItemUpdate;
import com.lightstreamer.client.LightstreamerClient;
import com.lightstreamer.client.Subscription;
import com.lightstreamer.client.SubscriptionListener;

public class LightstreamerConsumer {

    private static final Logger logger = LoggerFactory.getLogger(LightstreamerConsumer.class);

    /**
     * Command line arguments configuration using JCommander
     */
    public static class Args {
        @Parameter(names = { "-s",
                "--server" }, description = "Lightstreamer server URL (e.g., http://localhost:8080)", required = true)
        public String serverAddress;

        @Parameter(names = { "-f",
                "--from-key" }, description = "Starting key number for item subscription range", required = true)
        public Integer fromKey;

        @Parameter(names = { "-t",
                "--to-key" }, description = "Ending key number for item subscription range", required = true)
        public Integer toKey;

        @Parameter(names = { "-h", "--help" }, description = "Show this help message", help = true)
        public boolean help = false;

        /**
         * Validates the parsed arguments
         */
        public void validate() {
            if (fromKey < 0) {
                throw new ParameterException("from-key must be >= 0, got: " + fromKey);
            }
            if (toKey < 0) {
                throw new ParameterException("to-key must be >= 0, got: " + toKey);
            }
            if (fromKey > toKey) {
                throw new ParameterException(
                        "from-key must be <= to-key, got: from-key=" + fromKey + ", to-key=" + toKey);
            }
        }
    }

    /**
     * Prints comprehensive usage information with JCommander support
     */
    private static void printUsage(JCommander commander) {
        String header = """

                Lightstreamer Consumer - Kafka Connector Load Test
                ================================================

                DESCRIPTION:
                  Connects to a Lightstreamer server and subscribes to a range of items
                  to measure latency and performance metrics. Displays real-time latency
                  reports including client-side and server-side latency statistics.
                """;

        String examples = """

                EXAMPLES:
                  # Named parameters (recommended)
                  java -jar ls-consumer.jar --server http://localhost:8080 --from-key 0 --to-key 99
                  java -jar ls-consumer.jar -s http://myserver:8080 -f 0 -t 999

                  # Positional parameters (legacy compatibility)
                  java -jar ls-consumer.jar http://localhost:8080 0 99
                """;

        String behavior = """

                BEHAVIOR:
                  - Subscribes to items with pattern: ltest-[key=META250801P00680XXX]
                  - Where XXX is a 3-digit zero-padded number from <from-key> to <to-key>
                  - Reports latency statistics every 10,000 messages
                  - Tracks both client-side and latency metrics
                  - Uses percentiles: 50th, 95th, 98th, 99th, and maximum latency
                """;

        System.out.print(header);

        // Use JCommander's built-in usage formatting
        commander.usage();

        System.out.print(examples);
        System.out.println(behavior);
    }

    public static void main(String[] args) {
        Args cliArgs = new Args();
        JCommander commander = JCommander.newBuilder()
                .addObject(cliArgs)
                .programName("ls-consumer")
                .build();

        try {
            // Handle legacy positional arguments for backward compatibility
            if (args.length == 3 && !args[0].startsWith("-")) {
                // Convert positional args to named args
                String[] namedArgs = {
                        "--server", args[0],
                        "--from-key", args[1],
                        "--to-key", args[2]
                };
                commander.parse(namedArgs);
            } else {
                commander.parse(args);
            }

            if (cliArgs.help) {
                printUsage(commander);
                System.exit(0);
            }

            cliArgs.validate();

        } catch (ParameterException e) {
            System.err.println("❌ Error: " + e.getMessage());
            System.err.println();
            printUsage(commander);
            System.exit(1);
        }

        logger.info("Starting Lightstreamer Consumer");
        logger.info("Server: {}", cliArgs.serverAddress);
        logger.info("Key range: from {} to {} ({} items)", cliArgs.fromKey, cliArgs.toKey,
                (cliArgs.toKey - cliArgs.fromKey + 1));

        // LightstreamerClient.setLoggerProvider(new
        // ConsoleLoggerProvider(ConsoleLogLevel.DEBUG));
        LightstreamerClient client = new LightstreamerClient(cliArgs.serverAddress, "KafkaConnector");
        client.addListener(new MyClientListener());
        client.connect();

        // Prepare item names based on provided key range
        String[] items = IntStream.range(cliArgs.fromKey, cliArgs.toKey + 1)
                .mapToObj(i -> String.format("ltest-[key=META250801P00680%03d]", i))
                .toArray(String[]::new);
        logger.info("Subscribing from item {} to item {}", items[0], items[items.length - 1]);

        // Define the fields to subscribe to
        String[] fields = { "volume", "high", "partition", "last", "offset", "low", "sym", "ask", "bid", "tradetime",
                "timestamp", "route-latency" };

        Subscription sub = new Subscription("DISTINCT", items, fields);
        sub.setDataAdapter("QuickStart");
        sub.setRequestedSnapshot("no");
        sub.addListener(new LatencyDumper());
        sub.setRequestedMaxFrequency("unfiltered");
        client.subscribe(sub);
        logger.info("Subscribed to {} items", items.length);
    }

    private static class LatencyDumper implements SubscriptionListener {

        private static final int REPORT_INTERVAL_MESSAGE_COUNT = 10_000;

        private final Histogram clientLatencyHdr;
        private int intervalMessageCounter = 0;
        private PrintWriter out;

        Executor executor = Executors.newSingleThreadExecutor();

        public LatencyDumper() {
            this.clientLatencyHdr = new Histogram(3_600_000_000_000L, 3); // up to 1h, 3 digits
            try {
                this.out = new PrintWriter(new BufferedWriter(new FileWriter("offsets_received.txt", true)));
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    if (out != null) {
                        out.close();
                    }
                }));
            } catch (IOException e) {
                e.printStackTrace();
            }
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

        private void appendOffsetToFile(long offset) {
            out.println(offset);
            out.flush();
        }

        @Override
        public void onItemUpdate(ItemUpdate update) {
            logger.debug("Msg received: {}", update);
            long currentTimestamp = System.currentTimeMillis();
            long receivedTimestamp = Long.parseLong(update.getValue("timestamp"));
            long latency = currentTimestamp - receivedTimestamp;
            clientLatencyHdr.recordValue(latency);

            intervalMessageCounter++;
            if (intervalMessageCounter == REPORT_INTERVAL_MESSAGE_COUNT) {
                printLatencyReport(clientLatencyHdr);
                intervalMessageCounter = 0;
            }
            try {
                String offset = update.getValue("offset");
                try {
                    long receivedOffset = Long.parseLong(update.getValue("offset"));
                    executor.execute(() -> this.appendOffsetToFile(receivedOffset));

                } catch (NumberFormatException nfe) {
                    logger.error("Invalid offset format: {}", offset);
                }
            } catch (IllegalArgumentException iae) {
                logger.error("Offset field is missing in the update: {}", update);
            }
        }

        private void printLatencyReport(Histogram histogram) {
            logger.info("---- ClientLatency report ----");
            logger.info("Number of samples: {}", clientLatencyHdr.getTotalCount());
            printLatency(clientLatencyHdr, 50);
            printLatency(clientLatencyHdr, 95);
            printLatency(clientLatencyHdr, 98);
            printLatency(clientLatencyHdr, 99);
            printLatencyMax(clientLatencyHdr);
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
