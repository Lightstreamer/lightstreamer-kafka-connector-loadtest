package com.lightstreamer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lightstreamer.client.ItemUpdate;
import com.lightstreamer.client.SubscriptionListener;

public class mySubListener implements SubscriptionListener {

    private static final Logger logger = LogManager.getLogger(mySubListener.class);

    private StatisticsCalculator stats = new StatisticsCalculator(10000);

    private int k = 0;

    private long timediff(String timestampString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime timestamp = LocalDateTime.parse(timestampString, formatter);
        LocalDateTime oraAttuale = LocalDateTime.now();

        long differenzaMillisecondi = Duration.between(timestamp, oraAttuale).toMillis();

        return differenzaMillisecondi;
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

        logger.info("Value: " + update.getValue("value"));
        String tsmsg = update.getValue("value").substring(0, 23);
        long diff = timediff(tsmsg);
        stats.addValue(diff);

        // Iterator<Entry<String, String>> changedValues =
        // update.getChangedFields().entrySet().iterator();
        // while (changedValues.hasNext()) {
        // Entry<String, String> field = changedValues.next();
        // // logger.debug("Field " + field.getKey() + " changed: " + field.getValue());
        // // String tsmsg = field.getValue().substring(0, 23);

        // long diff = timediff(tsmsg);
        // stats.addValue(diff);
        // logger.debug("Key: " + update.getValue("key") + ", Value: " +
        // update.getValue("value"));
        // logger.debug("------------------- " + diff);
        // if (k == 0) {
        // logger.info("Mean: " + stats.calculateMean() + ", Median = " +
        // stats.calculateMedian()
        // + ", confidence = " + stats.calculateConfidenceInterval(500));
        // }
        // if (++k == 100)
        // k = 0;
        // }
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
