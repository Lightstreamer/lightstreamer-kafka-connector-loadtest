#!/bin/bash
#mvn clean package dependency:copy-dependencies
TOPIC=LTest
PRODUCER=2
PAUSE=50
MESSAGE_SIZE=1024
java -cp "target/kafkaconnector_tools-1.0-SNAPSHOT.jar:target/dependency/*:dependency/log4j2.xml" com.lightstreamer.MessageGenerator ec2-54-74-235-5.eu-west-1.compute.amazonaws.com:9092 $TOPIC $PRODUCER $PAUSE $MESSAGE_SIZE protobuf