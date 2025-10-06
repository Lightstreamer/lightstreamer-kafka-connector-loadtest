#!/bin/bash
java -jar target/ls-consumer.jar \
    --server "http://${KAFKA_LS_PUBLIC_IP}:8080" \
    --from-key $1 \
    --to-key $2
