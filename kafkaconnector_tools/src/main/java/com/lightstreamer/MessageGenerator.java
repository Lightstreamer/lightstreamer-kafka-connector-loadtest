package com.lightstreamer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageGenerator {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        int num_producers = 1;
        String kconnstring;
        String topicname;

        logger.info("Start Kafka demo producer: " + args.length);

        if (args.length < 2) {
            logger.error("Missing arguments bootstrap-servers topic-name");
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

        BaseProducer[] producers = new BaseProducer[num_producers];

        for (int k = 0; k < num_producers; k++) {
            producers[k] = new BaseProducer(kconnstring, "pid-" + k, topicname);
            producers[k].start();

            logger.info("Producer pid-" + k + " started.");
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
