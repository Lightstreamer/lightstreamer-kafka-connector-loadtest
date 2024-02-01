package com.lightstreamer;

public class Main {

    private static String kconnstring = "";

    private static String kconsumergroupid = "kcg-";

    private static String ktopicname = "";

    private static int num_consumers = 2;

    public static void main(String[] args) {
        BaseConsumer[] consumers;

        System.out.println("Hello world!");

        if (args.length < 3) {
            System.out.println("Missing arguments bootstrap-servers topic-name number-of-consumer");
            return;
        }

        kconnstring = args[0];
        ktopicname = args[1];
        try {
            num_consumers = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.err.println("Impossibile convertire in int. Assicurati che l'argomento sia un numero valido.");
        }
        System.out.println("number of consumers : " + num_consumers);

        consumers = new BaseConsumer[num_consumers];

        for (int k = 0; k < num_consumers; k++) {
            consumers[k] = new BaseConsumer(kconnstring, kconsumergroupid + k, ktopicname);
            consumers[k].start();

            System.out.println("Group id " + kconsumergroupid + k + " started.");
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