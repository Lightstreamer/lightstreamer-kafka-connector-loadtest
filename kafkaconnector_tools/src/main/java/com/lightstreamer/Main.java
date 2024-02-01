package com.lightstreamer;

public class Main {

    private static String kconnstring = "b-2.democluster1.rw4f0s.c9.kafka.eu-west-1.amazonaws.com:9092,b-1.democluster1.rw4f0s.c9.kafka.eu-west-1.amazonaws.com:9092";

    private static String kconsumergroupid = "kcg-";

    private static String ktopicname = "ciao";

    public static void main(String[] args) {
        System.out.println("Hello world!");

        for (int k = 0; k < 1; k++) {
            BaseConsumer consumer = new BaseConsumer(kconnstring, kconsumergroupid + k, ktopicname);
            consumer.start();
        }

    }
}