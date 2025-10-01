#!/bin/bash
#mvn clean package dependency:copy-dependencies
TOPIC=LTest
PRODUCERS=1
# KAFKA_CLUSTER=localhost:9092
KAFKA_CLUSTER=ec2-18-201-235-33.eu-west-1.compute.amazonaws.com:9092
java -cp "target/original-kafkaconnector_tools-1.0.1-SNAPSHOT.jar:target/dependency/*" com.lightstreamer.MessageGenerator \
    ${KAFKA_CLUSTER} \
    $TOPIC \
    $PRODUCERS 500 1024 \
    protobuf
