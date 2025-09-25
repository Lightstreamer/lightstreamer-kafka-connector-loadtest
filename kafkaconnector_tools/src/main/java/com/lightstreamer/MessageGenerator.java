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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageGenerator {

    private static final Logger logger = LoggerFactory.getLogger(MessageGenerator.class);

    public static void main(String[] args) {
        int num_producers = 1;
        int pause_milis = 1000;
        int msg_size = 1024;
        String kconnstring;
        String topicname;

        logger.info("Start kafka message generator: {} ...", args.length);

        if (args.length < 6) {
            logger.error(
                    "Missing arguments: <bootstrap servers connection string> <topic name> <number of producers> <wait pause in millis> <msg size>");
            return;
        }

        kconnstring = args[0];
        topicname = args[1];

        try {
            num_producers = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            logger.error("Unable to convert to integer. Make sure the argument %d is a valid number.", 2);
        }
        logger.info("\t\t number of producers : {}", num_producers);

        try {
            pause_milis = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            logger.error("Unable to convert to integer. Make sure the argument %d is a valid number.", 3);
        }
        logger.info("\t\t wait pause in millis : {}", pause_milis);

        try {
            msg_size = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            logger.error("Unable to convert to integer. Make sure the argument %d is a valid number.", 4);
        }
        logger.info("\t\t message size requested : {}", msg_size);

        String keyornot = args[5];

        boolean additionalParam = false;
        if (args.length > 6) {
            additionalParam = Boolean.parseBoolean(args[6]);
        }

        boolean lastParamForJson = false;
        if (args.length > 7) {
            lastParamForJson = Boolean.parseBoolean(args[7]);
        }

        BaseProducer[] producers = new BaseProducer[num_producers];

        if (keyornot.equals("keyed")) {
            for (int k = 0; k < num_producers; k++) {
                producers[k] = new KeyProducer(kconnstring, "pid-" + k, topicname, pause_milis, msg_size,
                        additionalParam);
                producers[k].start();

                logger.info("Key Producer pid-{} started.", k);
            }
        } else if (keyornot.equals("json")) {
            for (int k = 0; k < num_producers; k++) {
                producers[k] = new JsonProducer(kconnstring, "pid-" + k, topicname, pause_milis, msg_size,
                        additionalParam, lastParamForJson);
                producers[k].start();

                logger.info("Json Producer pid-{} started.", k);
            }
        } else if (keyornot.equals("protobuf")) {
            for (int k = 0; k < num_producers; k++) {
                producers[k] = new ProtobufProducer(kconnstring, "pid-" + k, topicname, pause_milis, msg_size,
                        additionalParam, lastParamForJson);
                producers[k].start();

                logger.info("Protobuf Producer pid-{} started.", k);
            }
        } else if (keyornot.equals("complex")) {
            producers[0] = new JsonComplexProducer(kconnstring, "pid-0", topicname, pause_milis, msg_size, true);
            producers[0].start();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // .
            }

            for (int k = 1; k < num_producers; k++) {
                producers[k] = new JsonComplexProducer(kconnstring, "pid-" + k, topicname, pause_milis, msg_size,
                        false);
                producers[k].start();

                logger.info("Json Producer pid-{} started.", k);
            }
        } else if (keyornot.equals("verycomplex")) {
            producers[0] = new JsonVeryComplexProducer(kconnstring, "pid-0", topicname, pause_milis, msg_size, true,
                    additionalParam);
            producers[0].start();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // .
            }

            for (int k = 1; k < num_producers; k++) {
                producers[k] = new JsonVeryComplexProducer(kconnstring, "pid-" + k, topicname, pause_milis, msg_size,
                        false, additionalParam);
                producers[k].start();

                logger.info("Json Complex Producer (very) pid-{} started.", k);
            }
        } else if (keyornot.equals("jsonkeyx")) {
            producers[0] = new JsonVeryComplexProducerWithJsonKey(kconnstring, "pid-0", topicname, pause_milis,
                    msg_size, true, additionalParam);
            producers[0].start();

            logger.info("Json Key Complex Producer (very) pid-{} started.", 0);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // .
            }

            for (int k = 1; k < num_producers; k++) {
                producers[k] = new JsonVeryComplexProducerWithJsonKey(kconnstring, "pid-" + k, topicname, pause_milis,
                        msg_size, false, additionalParam);
                producers[k].start();

                logger.info("Json Key Complex Producer (very) pid-{} started.", k);
            }
        } else {
            for (int k = 0; k < num_producers; k++) {
                producers[k] = new BaseProducer(kconnstring, "pid-" + k, topicname, pause_milis, msg_size);
                producers[k].start();

                logger.info("Producer pid-{} started.", k);
            }
        }

        String input = System.console().readLine();
        while (!input.equalsIgnoreCase("stop")) {
            input = System.console().readLine();
            if (input == null)
                input = "";
        }
        for (int j = 0; j < num_producers; j++)
            producers[j].stopproducing();

        logger.info("End kafka message generator.");
    }
}
