#!/bin/bash
set -ex
#mvn clean package dependency:copy-dependencies
KAFKA_CLUSTER=ec2-3-250-39-145.eu-west-1.compute.amazonaws.com:9092
TOPIC=LTest
CONSUMERS=1
CONSUMER_GROUP_ID=simple1
java -cp "target/kafkaconnector_tools-1.0-SNAPSHOT.jar:target/dependency/*:dependency/log4j2.xml" com.lightstreamer.Main \
    $KAFKA_CLUSTER \
    $TOPIC \
    $CONSUMERS \
    $CONSUMER_GROUP_ID \
    true
