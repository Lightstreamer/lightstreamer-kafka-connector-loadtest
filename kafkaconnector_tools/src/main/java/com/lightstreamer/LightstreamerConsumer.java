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

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lightstreamer.client.LightstreamerClient;
import com.lightstreamer.client.Subscription;

public class LightstreamerConsumer {

    private static StatisticsManager statsManager = null;

    private static final Logger logger = LoggerFactory.getLogger(LightstreamerConsumer.class);

    public static void main(String[] args) {
        boolean calculateLatencyStats = false;
        boolean isKJ = false;
        boolean extkey = false;
        String serverAddress = "http://localhost:8080/";

        logger.info("Args: {} ...", Arrays.toString(args));

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

        // String[] items = { "ltest-[key=Banana]" };
        String[] items = { "ltest"
                // "ltest-[key=Apple]",
                // "ltest-[key=Lime]",
                // "ltest-[key=Starfruit]",
                // "ltest-[key=Durian]",
                // "ltest-[key=Jackfruit]",
                // "ltest-[key=Kumquat]",
                // "ltest-[key=Rambutan]",
                // "ltest-[key=Mangosteen]",
                // "ltest-[key=Dragonfruit]",
                // "ltest-[key=Passionfruit]",
                // "ltest-[key=Papaya]",
                // "ltest-[key=Watermelon]",
                // "ltest-[key=Strawberry]",
                // "ltest-[key=Orange]",
                // "ltest-[key=Cherry]",
                // "ltest-[key=Blueberry]",
                // "ltest-[key=Raspberry]",
                // "ltest-[key=Peach]",
                // "ltest-[key=Plum]",
                // "ltest-[key=Pear]",
                // "ltest-[key=Avocado]",
                // "ltest-[key=Lychee]",
                // "ltest-[key=Guava]",
                // "ltest-[key=Banana]",
                // "ltest-[key=Grape]",
                // "ltest-[key=Pineapple]",
                // "ltest-[key=Blackberry]",
                // "ltest-[key=Coconut]",
                // "ltest-[key=Melon]",
                // "ltest-[key=Lemon]",
                // "ltest-[key=Apricot]",
                // "ltest-[key=Fig]",
                // "ltest-[key=Pomegranate]",
                // "ltest-[key=Cantaloupe]",
                // "ltest-[key=Tangerine]",
                // "ltest-[key=Cranberry]"
        };
        // String[] fields = { "timestamp", "fstValue", "sndValue", "intNum" };
        String[] fields = { "timestamp" };

        Subscription sub = new Subscription("DISTINCT", items, fields);
        sub.setDataAdapter("QuickStart");
        sub.setRequestedSnapshot("no");
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
