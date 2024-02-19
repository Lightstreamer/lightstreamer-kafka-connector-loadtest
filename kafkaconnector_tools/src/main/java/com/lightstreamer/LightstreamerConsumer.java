package com.lightstreamer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lightstreamer.client.LightstreamerClient;
import com.lightstreamer.client.Subscription;

public class LightstreamerConsumer {

    private static final Logger logger = LogManager.getLogger(LightstreamerConsumer.class);
    public static void main(String[] args) {
        LightstreamerClient client = new LightstreamerClient(
                "http://ec2-34-248-222-182.eu-west-1.compute.amazonaws.com:8080/", "KafkaConnector");
        // "http://ec2-18-201-206-246.eu-west-1.compute.amazonaws.com:8080/",
        // "KafkaConnector");
        client.addListener(new MyClientListener());
        client.connect();

        logger.info("Subscribe to Kafka Topic MSKTest1.");

        String[] items = { "ltest-<key=orange>" };
        String[] fields = { "key", "value", "ts", "partition" };
        Subscription sub = new Subscription("RAW", items, fields);
        sub.setDataAdapter("QuickStart");
        // sub.setRequestedSnapshot("no");
        sub.addListener(new mySubListener());
        client.subscribe(sub);

        String input = System.console().readLine();
        while (!input.equalsIgnoreCase("stop")) {
            input = System.console().readLine();
            if (input == null)
                input = "";
        }

        return;
    }
}
