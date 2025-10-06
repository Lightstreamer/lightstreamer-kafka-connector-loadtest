#/bin/bash
java -jar target/ls-consumer.jar --server "http://${KAFKA_LS_PUBLIC_IP}:8080" --from-key 0 --to-key 99 &
java -jar target/ls-consumer.jar --server "http://${KAFKA_LS_PUBLIC_IP}:8080" --from-key 100 --to-key 199 &
java -jar target/ls-consumer.jar --server "http://${KAFKA_LS_PUBLIC_IP}:8080" --from-key 200 --to-key 299 &