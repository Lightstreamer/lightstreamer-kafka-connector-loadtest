package com.lightstreamer;

import java.util.Iterator;
import java.util.Map.Entry;

import com.lightstreamer.client.ItemUpdate;
import com.lightstreamer.client.SubscriptionListener;

public class mySubListener implements SubscriptionListener {

    @Override
    public void onClearSnapshot(String itemName, int itemPos) {
        System.out.println("Server has cleared the current status of the chat");
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
        System.out.println("Snapshot is now fully received, from now on only real-time messages will be received");
    }

    @Override
    public void onItemLostUpdates(String itemName, int itemPos, int lostUpdates) {
        System.out.println(lostUpdates + " messages were lost");
    }

    @Override
    public void onItemUpdate(ItemUpdate update) {

        System.out.println("====UPDATE====> " + update.getItemName());

        Iterator<Entry<String, String>> changedValues = update.getChangedFields().entrySet().iterator();
        while (changedValues.hasNext()) {
            Entry<String, String> field = changedValues.next();
            System.out.println("Field " + field.getKey() + " changed: " + field.getValue());
        }

        System.out.println("<====UPDATE====");
    }

    @Override
    public void onListenEnd() {
        System.out.println("Stop listeneing to subscription events");
    }

    @Override
    public void onListenStart() {
        System.out.println("Start listeneing to subscription events");
    }

    @Override
    public void onSubscription() {
        System.out.println("Now subscribed to the chat item, messages will now start coming in");
    }

    @Override
    public void onSubscriptionError(int code, String message) {
        System.out.println("Cannot subscribe because of error " + code + ": " + message);
    }

    @Override
    public void onUnsubscription() {
        System.out.println("Now unsubscribed from chat item, no more messages will be received");
    }

    @Override
    public void onRealMaxFrequency(String frequency) {
        System.out.println("Frequency is " + frequency);
    }

}
