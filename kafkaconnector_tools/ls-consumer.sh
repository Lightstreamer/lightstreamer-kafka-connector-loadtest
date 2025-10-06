#!/bin/bash
java -jar target/ls-consumer.jar "http://${KAFKA_LS_PUBLIC_IP}:8080" $1 $2
