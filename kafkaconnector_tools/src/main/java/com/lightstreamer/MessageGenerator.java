package com.lightstreamer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageGenerator {

    private static Logger logger = LogManager.getLogger("kafkademo-producer");

    private static String kconnstring;

    private static String topicname;

    public static void main(String[] args) {
        int num_producers = 1;

        System.out.println("Start Kafka demo producer: " + args.length);

        if (args.length < 2) {
            System.out.println("Missing arguments bootstrap-servers topic-name");
            return;
        }

        kconnstring = args[0];
        topicname = args[1];

        try {
            num_producers = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.err.println("Impossibile convertire in int. Assicurati che l'argomento sia un numero valido.");
        }
        System.out.println("number of producers : " + num_producers);

        BaseProducer[] producers = new BaseProducer[num_producers];

        for (int k = 0; k < num_producers; k++) {
            producers[k] = new BaseProducer(kconnstring, "pid-" + k, topicname);
            producers[k].start();

            System.out.println("Producer pid-" + k + " started.");
        }

        String input = System.console().readLine();
        while (!input.equalsIgnoreCase("stop")) {
            input = System.console().readLine();
            if (input == null)
                input = "";
        }
        for (int j = 0; j < num_producers; j++)
            producers[j].stopproducing();

        System.out.println("End Kafka demo producer.");
    }
}
