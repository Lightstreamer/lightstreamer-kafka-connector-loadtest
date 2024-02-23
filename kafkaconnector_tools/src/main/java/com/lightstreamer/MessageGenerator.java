package com.lightstreamer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageGenerator {

    private static final Logger logger = LogManager.getLogger(MessageGenerator.class);

    public static void main(String[] args) {
        int num_producers = 1;
        int pause_milis = 1000;
        int msg_size = 1024;
        String kconnstring;
        String topicname;

        logger.info("Start Kafka demo producer: " + args.length);

        if (args.length < 6) {
            logger.error(
                    "Missing arguments: <bootstrap servers connection string> <topic name> <number of producers> <wait pause in millis>");
            return;
        }

        kconnstring = args[0];
        topicname = args[1];

        try {
            num_producers = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            logger.error("Impossibile convertire in int. Assicurati che l'argomento sia un numero valido.");
        }
        logger.info("number of producers : " + num_producers);

        try {
            pause_milis = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            logger.error("Impossibile convertire in int. Assicurati che l'argomento sia un numero valido.");
        }
        logger.info("wait pause in millis : " + pause_milis);

        try {
            msg_size = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            logger.error("Impossibile convertire in int. Assicurati che l'argomento sia un numero valido.");
        }
        logger.info("message size requested : " + msg_size);

        String keyornot = args[5];

        BaseProducer[] producers = new BaseProducer[num_producers];

        if (keyornot.equals("keyed")) {
            for (int k = 0; k < num_producers; k++) {
                producers[k] = new KeyProducer(kconnstring, "pid-" + k, topicname, pause_milis, msg_size);
                producers[k].start();

                logger.info("Key Producer pid-" + k + " started.");
            }
        } else if (keyornot.equals("json")) {
            for (int k = 0; k < num_producers; k++) {
                producers[k] = new JsonProducer(kconnstring, "pid-" + k, topicname, pause_milis, msg_size);
                producers[k].start();

                logger.info("Json Producer pid-" + k + " started.");
            }
        } else {
            for (int k = 0; k < num_producers; k++) {
                producers[k] = new BaseProducer(kconnstring, "pid-" + k, topicname, pause_milis, msg_size);
                producers[k].start();

                logger.info("Producer pid-" + k + " started.");
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

        logger.info("End Kafka demo producer.");
    }
}
