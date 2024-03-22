package com.lightstreamer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lightstreamer.client.LightstreamerClient;
import com.lightstreamer.client.Subscription;

public class LightstreamerConsumer {

    private static final Logger logger = LogManager.getLogger(LightstreamerConsumer.class);
    public static void main(String[] args) {
        LightstreamerClient client = new LightstreamerClient(
                "http://ec2-34-245-21-151.eu-west-1.compute.amazonaws.com:8080/", "KafkaConnector");
        // "http://ec2-18-201-206-246.eu-west-1.compute.amazonaws.com:8080/",
        // "KafkaConnector");
        client.addListener(new MyClientListener());
        client.connect();

        logger.info("Subscribe to Kafka Topic jsontest.");

        // String[] items = { "jsontest-<sndValue=Pineapple>" };
        // String[] items = { "jsontest-<timestamp=Pineapple>" };
        String[] items = { "jsontest-[sndValue=Pineapple]" };
        String item = "ltest";

        // String[] fields = { "key", "timestamp", "sndValue", "intNum", "ts",
        // "partition" };
        String[] fields = { "key", "value" };
        Subscription sub = new Subscription("RAW", item, fields);
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
