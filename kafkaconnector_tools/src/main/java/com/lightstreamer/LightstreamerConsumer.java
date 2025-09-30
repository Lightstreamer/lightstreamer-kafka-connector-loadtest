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

        String[] items = {
                "ltest-[key=META250801P00680000]",
                "ltest-[key=META250801P00680001]",
                "ltest-[key=META250801P00680002]",
                "ltest-[key=META250801P00680003]",
                "ltest-[key=META250801P00680004]",
                "ltest-[key=META250801P00680005]",
                "ltest-[key=META250801P00680006]",
                "ltest-[key=META250801P00680007]",
                "ltest-[key=META250801P00680008]",
                "ltest-[key=META250801P00680009]",
                "ltest-[key=META250801P00680010]",
                "ltest-[key=META250801P00680011]",
                "ltest-[key=META250801P00680012]",
                "ltest-[key=META250801P00680013]",
                "ltest-[key=META250801P00680014]",
                "ltest-[key=META250801P00680015]",
                "ltest-[key=META250801P00680016]",
                "ltest-[key=META250801P00680017]",
                "ltest-[key=META250801P00680018]",
                "ltest-[key=META250801P00680019]",
                "ltest-[key=META250801P00680020]",
                "ltest-[key=META250801P00680021]",
                "ltest-[key=META250801P00680022]",
                "ltest-[key=META250801P00680023]",
                "ltest-[key=META250801P00680024]",
                "ltest-[key=META250801P00680025]",
                "ltest-[key=META250801P00680026]",
                "ltest-[key=META250801P00680027]",
                "ltest-[key=META250801P00680028]",
                "ltest-[key=META250801P00680029]",
                "ltest-[key=META250801P00680030]",
                "ltest-[key=META250801P00680031]",
                "ltest-[key=META250801P00680032]",
                "ltest-[key=META250801P00680033]",
                "ltest-[key=META250801P00680034]",
                "ltest-[key=META250801P00680035]",
                "ltest-[key=META250801P00680036]",
                "ltest-[key=META250801P00680037]",
                "ltest-[key=META250801P00680038]",
                "ltest-[key=META250801P00680039]"
        };
        // String[] fields = { "timestamp", "fstValue", "sndValue", "intNum" };
        String[] fields = { "volume", "high", "partition", "last", "offset", "low", "sym", "ask", "bid", "tradetime" };

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
