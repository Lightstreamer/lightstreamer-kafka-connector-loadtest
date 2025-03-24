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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lightstreamer.client.ItemUpdate;
import com.lightstreamer.client.SubscriptionListener;

public class MySubListener implements SubscriptionListener {

    private static final Logger logger = LogManager.getLogger(MySubListener.class);

    private StatisticsManager statsManager;
    private boolean calculateLatencyStats;
    private boolean kj;
    
        public MySubListener(boolean calculateLatencyStats, StatisticsManager statsManager, boolean kj) {
            this.calculateLatencyStats = calculateLatencyStats;
            this.statsManager = statsManager;
            this.kj = kj;
    }

    private int k = 0;

    private int timediff(String timestampString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime timestamp = LocalDateTime.parse(timestampString, formatter);
        LocalDateTime oraAttuale = LocalDateTime.now();

        long differenzaMillisecondi = Duration.between(timestamp, oraAttuale).toMillis();

        return (int) differenzaMillisecondi;
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
        logger.info(lostUpdates + " messages were lost");
    }

    @Override
    public void onItemUpdate(ItemUpdate update) {

        /*
         * String tsmsg = update.getValue("timestamp");
         * long diff = timediff(tsmsg);
         * stats.addValue(diff);
         * logger.debug("2ndValue: " + update.getValue("sndValue"));
         * logger.debug("------------------- " + diff);
         */
        /*
         * logger.info("Value: " + update.getValue("value"));
         * String tsmsg = update.getValue("value").substring(0, 23);
         * long diff = timediff(tsmsg);
         * stats.addValue(diff);
         */

        String updts = update.getValue("timestamp");
        if (kj) {
            logger.debug(" --> " + updts + " - " + update.getValue("secondText") + " - " + update.getValue("thirdNumber") + " - " + update.getValue("hobbie1"));
            logger.debug(" key: " + update.getValue("key") + " - " + update.getValue("names775") + " - " + update.getValue("names1001"));
        } else {
            logger.debug(" --> " + updts + " - " + update.getValue("fstValue") + " - " + update.getValue("intNum") + " - " + update.getValue("sndValue"));
            logger.debug(" key: " + update.getValue("key") );
        }
        
        if (calculateLatencyStats && updts.startsWith("PREFIX-")) {
            String tsmsg = updts.substring(7, 30); // Skip the "PREFIX-" part

            int diff = timediff(tsmsg);
            
            this.statsManager.onData(diff);
            logger.debug("------------------- " + diff);

            if (k == 0) {
                statsManager.generateReport();
            }
            if (++k == 10) k = 0;
        }
        /* 
                 Iterator<Entry<String, String>> changedValues = update.getChangedFields().entrySet().iterator();
        while (changedValues.hasNext()) {
            Entry<String, String> field = changedValues.next();
            logger.debug("Field " + field.getKey() + " changed: " + field.getValue());

            if (calculateLatencyStats && field.getValue().startsWith("PREFIX-")) {
                String tsmsg = field.getValue().substring(7, 30); // Skip the "PREFIX-" part

                long diff = timediff(tsmsg);
                stats.addValue(diff);
                logger.debug("------------------- " + diff);

                if (k == 0) {
                    logger.info("Mean: " + stats.calculateMean() + ", Median = " +
                        stats.calculateMedian()
                        + ", confidence = " + stats.calculateConfidenceInterval(500));
                }
                if (++k == 10) k = 0;
            }
        }
         */


    }

    @Override
    public void onListenEnd() {
        logger.info("Stop listeneing to subscription events");
    }

    @Override
    public void onListenStart() {
        logger.info("Start listeneing to subscription events");
    }

    @Override
    public void onSubscription() {
        logger.info("Now subscribed to the chat item, messages will now start coming in");
    }

    @Override
    public void onSubscriptionError(int code, String message) {
        logger.info("Cannot subscribe because of error " + code + ": " + message);
    }

    @Override
    public void onUnsubscription() {
        logger.info("Now unsubscribed from chat item, no more messages will be received");
    }

    @Override
    public void onRealMaxFrequency(String frequency) {
        logger.info("Frequency is " + frequency);
    }

}
