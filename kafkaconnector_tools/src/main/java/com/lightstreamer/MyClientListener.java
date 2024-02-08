package com.lightstreamer;

import com.lightstreamer.client.ClientListener;

public class MyClientListener implements ClientListener {

    @Override
    public void onListenEnd() {
        System.out.println("Stops listening to client events");
    }

    @Override
    public void onListenStart() {
        System.out.println("Start listening to client events");

    }

    @Override
    public void onPropertyChange(String property) {
        System.out.println("Client property changed: " + property);
    }

    @Override
    public void onServerError(int code, String message) {
        System.out.println("Server error: " + code + ": " + message);
    }

    @Override
    public void onStatusChange(String newStatus) {
        System.out.println("Connection status changed to " + newStatus);
    }

}
