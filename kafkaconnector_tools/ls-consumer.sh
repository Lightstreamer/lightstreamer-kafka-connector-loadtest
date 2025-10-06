#!/bin/bash
java -jar target/ls-consumer.jar \
    --server "http://${KAFKA_LS_PUBLIC_IP}:8080" \
    --clients $1 \
    --keys $2
