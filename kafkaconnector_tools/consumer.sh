#!/bin/bash
java -cp "target/kafkaconnector_tools-1.0-SNAPSHOT.jar:target/dependency/*:dependency/log4j2.xml" \
    com.lightstreamer.LightstreamerConsumer --server-address=http://ec2-34-244-144-102.eu-west-1.compute.amazonaws.com:8080 \
    --calculate-latency-stats
