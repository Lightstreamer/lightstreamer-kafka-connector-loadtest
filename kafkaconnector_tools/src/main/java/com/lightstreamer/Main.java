package com.lightstreamer;

import org.apache.kafka.common.config.ConfigException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private static String kconnstring = "";

    private static String kconsumergroupid = "ktcg-";

    private static String ktopicname = "";

    private static int num_consumers = 2;

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        BaseConsumer[] consumers;

        logger.info("Main consumer test started.");

        if (args.length < 3) {
            logger.error("Missing arguments bootstrap-servers topic-name number-of-consumer");
            return;
        }

        kconnstring = args[0];
        ktopicname = args[1];
        try {
            num_consumers = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            logger.error("Impossibile convertire in int. Assicurati che l'argomento sia un numero valido.");
        }
        logger.info("number of consumers : " + num_consumers);

        consumers = new BaseConsumer[num_consumers];

        for (int k = 0; k < num_consumers; k++) {
            consumers[k] = new BaseConsumer(kconnstring, kconsumergroupid + k, ktopicname, k == 0);
            consumers[k].start();

            logger.info("Group id " + kconsumergroupid + k + " started.");
        }

        String input = System.console().readLine();
        while (!input.equalsIgnoreCase("stop")) {
            input = System.console().readLine();
            if (input == null)
                input = "";
        }

        for (int j = 0; j < num_consumers; j++)
            consumers[j].stopconsuming();

    }
}