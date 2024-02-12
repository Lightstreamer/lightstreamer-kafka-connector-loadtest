package com.lightstreamer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lightstreamer.client.ClientListener;

public class MyClientListener implements ClientListener {

    private static final Logger logger = LogManager.getLogger(MyClientListener.class);

    @Override
    public void onListenEnd() {
        logger.info("Stops listening to client events");
    }

    @Override
    public void onListenStart() {
        logger.info("Start listening to client events");
    }

    @Override
    public void onPropertyChange(String property) {
        logger.info("Client property changed: " + property);
    }

    @Override
    public void onServerError(int code, String message) {
        logger.info("Server error: " + code + ": " + message);
    }

    @Override
    public void onStatusChange(String newStatus) {
        logger.info("Connection status changed to " + newStatus);
    }

}
