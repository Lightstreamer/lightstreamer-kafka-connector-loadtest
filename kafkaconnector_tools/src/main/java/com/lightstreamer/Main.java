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
        StatisticsManager statsManager = null;

        logger.info("Main consumer test started.");

        if (args.length < 5) {
            logger.error(
                    "Missing arguments <bootstrap-servers> <topic-name> <number-of-consumer> <consumer group prefix> <is latency reporter>");
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

        kconsumergroupid = args[3];

        boolean flag = Boolean.parseBoolean(args[4]);
        logger.info("is latency reporter : " + flag);

        statsManager = new StatisticsManager();

        if (kconsumergroupid.equals("standalone")) {
            StandaloneConsumer[] consumers;
            consumers = new StandaloneConsumer[num_consumers];

            for (int k = 0; k < num_consumers; k++) {
                consumers[k] = new StandaloneConsumer(kconnstring, ktopicname, flag, statsManager);
                consumers[k].start();

                logger.info("Standalone consumer n. " + k + " started.");

                try {
                    Thread.sleep(15);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            String input = System.console().readLine();
            while (!input.equalsIgnoreCase("stop")) {
                input = System.console().readLine();
                if (input == null)
                    input = "";
            }

            for (int j = 0; j < num_consumers; j++)
                consumers[j].stopconsuming();
        } else if (kconsumergroupid.startsWith("json")) {
            JsonConsumer[] consumers;
            consumers = new JsonConsumer[num_consumers];

            for (int k = 0; k < num_consumers; k++) {
                consumers[k] = new JsonConsumer(kconnstring, kconsumergroupid + k, ktopicname, flag, statsManager);
                consumers[k].start();

                logger.info("Json consumer n. " + k + " started.");

                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            String input = System.console().readLine();
            while (!input.equalsIgnoreCase("stop")) {
                input = System.console().readLine();
                if (input == null)
                    input = "";
            }

            for (int j = 0; j < num_consumers; j++)
                consumers[j].stopconsuming();
        } else {
            BaseConsumer[] consumers;
            consumers = new BaseConsumer[num_consumers];

            for (int k = 0; k < num_consumers; k++) {
                consumers[k] = new BaseConsumer(kconnstring, kconsumergroupid + k, ktopicname, flag, statsManager);
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
}