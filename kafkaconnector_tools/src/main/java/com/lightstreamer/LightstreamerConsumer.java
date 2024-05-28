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

    private static final Logger logger = LogManager.getLogger(LightstreamerConsumer.class);
    public static void main(String[] args) {
        LightstreamerClient client = new LightstreamerClient(
                "http://ec2-3-254-55-163.eu-west-1.compute.amazonaws.com:8080/", "KafkaConnector");
        // "http://ec2-18-201-206-246.eu-west-1.compute.amazonaws.com:8080/",
        // "KafkaConnector");
        client.addListener(new MyClientListener());
        client.connect();

        logger.info("Subscribe to Kafka Topic.");

        // String[] items = { "jsontest-<sndValue=Pineapple>" };
        // String[] items = { "jsontest-<timestamp=Pineapple>" };
        // String[] items = { "jsontest-[key=Charles]", "jsontest-[key=James]",
        // "jsontest-[key=Michael]" };
        String[] items = { "ltest-[key=Charles]", "ltest-[key=Larry]" };

        // String[] fields = { "key", "timestamp", "sndValue", "intNum", "ts",
        // "partition" };
        String[] fields = { "key", "value" };

        // String[] fields = { "key", "firstText", "secondText", "thirdText",
        // "fourthText", "firstnumber",
        // "secondNumber", "thirdNumber", "fourthNumber", "hobbies", "timestamp" };

        Subscription sub = new Subscription("RAW", items, fields);
        sub.setDataAdapter("QuickStart");
        // sub.setDataAdapter("JsonStart-k");
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
