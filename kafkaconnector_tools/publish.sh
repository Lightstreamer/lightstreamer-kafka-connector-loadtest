#!/bin/bash
#mvn clean package dependency:copy-dependencies
TOPIC=LTest
PRODUCERS=1
KAFKA_CLUSTER=localhost:9092
java -cp "target/kafkaconnector_tools-1.0-SNAPSHOT.jar:target/dependency/*:dependency/log4j2.xml" com.lightstreamer.MessageGenerator \
    ${KAFKA_CLUSTER} \
    $TOPIC \
    $PRODUCERS 500 1024 \
    simple
