#!/bin/bash
set -ex

# Kafka Consumer using optimized consumer.jar
# Update these variables as needed for your environment
TOPIC=LTest
CONSUMERS=1
CONSUMER_GROUP_ID=protobuf

# Use the optimized consumer.jar instead of old classpath setup
java -jar target/consumer.jar \
    $KAFKA_CLUSTER \
    $TOPIC \
    $CONSUMERS \
    $CONSUMER_GROUP_ID \
    true
