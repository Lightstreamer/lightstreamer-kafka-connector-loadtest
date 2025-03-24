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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lightstreamer.client.LightstreamerClient;
import com.lightstreamer.client.Subscription;

public class LightstreamerConsumer {

    private static StatisticsManager statsManager = null; 

    private static final Logger logger = LogManager.getLogger(LightstreamerConsumer.class);
    public static void main(String[] args) {
        boolean calculateLatencyStats = false;
        boolean isKJ = false;
        boolean extkey = false;
        String serverAddress = "http://localhost:8080/";

        for (String arg : args) {
            if (arg.equalsIgnoreCase("--calculate-latency-stats")) {
                calculateLatencyStats = true;
                statsManager = new StatisticsManager();
            } else if (arg.startsWith("--server-address=")) {
                serverAddress = arg.split("=", 2)[1];
            } else if (arg.equalsIgnoreCase("--kj")) {
                isKJ = true;
            } else if (arg.equalsIgnoreCase("--extended-key")) {
                extkey = true;
            }
        }

        LightstreamerClient client = new LightstreamerClient(serverAddress, "KafkaConnector");
        client.addListener(new MyClientListener());
        client.connect();

        logger.info("Subscribe to Kafka Topic: " + (isKJ ? "KJ" : "LS") + " - " + (extkey ? "Extended Key" : "Simple Key"));

        
        // String[] items = { "ltest-[key=Banana]" };
        String item = extkey ? "ltest-[key=KiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwiKiwi]" : "ltest-[key=Banana]";
        String[] items = { item };
        String[] fields = { "key", "timestamp", "fstValue", "intNum", "sndValue" };
        
        String[] items_kj = { "ltest-[key=Timothy]" };
        String[] fields_kj = { "key", "changes", "timestamp", "secondText", "thirdNumber", "hobbie1", "names10", "names18", "names42", "names100", "names775", "names889", "names1001" };

        String dataAdapterName = isKJ ? "LoadTest_KJ" : "LoadTest";
        String[] selectedItems = isKJ ? items_kj : items;
        String[] selectedFields = isKJ ? fields_kj : fields;

        Subscription sub = new Subscription("DISTINCT", selectedItems, selectedFields);
        sub.setDataAdapter(dataAdapterName);
        sub.addListener(new MySubListener(calculateLatencyStats, statsManager, isKJ));
        sub.setRequestedMaxFrequency("unfiltered");
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
