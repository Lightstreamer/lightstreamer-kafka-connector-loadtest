# Lightstreamer vs. Kafka Benchmarking Tool

Welcome to the Lightstreamer vs. Kafka Benchmarking Tool!
This project provides a suite of programs designed to benchmark the performance of Lightstreamer and Kafka in handling high-volume data streams across thousands of clients.

Lightstreamer optimizes data delivery to clients through its real-time streaming engine, which efficiently manages and prioritizes data updates. By employing techniques like delta streaming and smart throttling, Lightstreamer minimizes bandwidth usage while ensuring timely delivery of relevant updates to connected clients. This approach allows Lightstreamer to scale seamlessly to support large numbers of concurrently connected clients, making it ideal for high-throughput real-time applications.

## Introduction

This benchmarking tool is intended to assist developers and system administrators in evaluating the real-time data streaming capabilities of Lightstreamer and Kafka. By simulating a large number of client connections and measuring various performance metrics, users can gain insights into the scalability, throughput, and latency characteristics of each platform.

The tool includes components for generating synthetic data streams, simulating client connections, and measuring key performance indicators such as message delivery latency, throughput, and system resource utilization.

## Features
* __Scalability Testing__: Simulate thousands of concurrent client connections to assess the scalability of Lightstreamer and Kafka.
* __Latency Measurement__: Measure end-to-end message delivery latency under varying load conditions.
* __Throughput Analysis__: Evaluate the maximum throughput achievable by each platform under different scenarios.
* __Resource Monitoring__: Monitor system resource utilization (CPU, memory, network) during benchmarking tests.

## Scearions of Test

We conducted a series of tests with various configurations to simulate different scenarios.

In the first scenario, we generated simple messages consisting of a single field with a string value of 1024 bytes.
Each message was accompanied by a basic header containing the creation timestamp and the producer's ID.
These messages were sent to Kafka without specifying a key, resulting in all clients receiving all messages.
Furthermore, there was no possibility of performing delta delivery on the message payload.

The second scenario replicated the message composition of the first scenario, but each message sent to Kafka also included a key value chosen from a set of 40 possibilities.
This allowed Lightstreamer clients to subscribe to a single item associated with a specific key, receiving only the messages relevant to that key.
In contrast, a generic Kafka client would receive all messages and have to determine which message to process based on the key.
The advantages in terms of network bandwidth savings are evident.

In the third scenario, we serialized JSON messages in Kafka, including the key value.
The message producer, for each key, would send a sequence of messages with variations relative to only a subset of the fields composing the JSON structure.
In addition to the benefits of targeted subscription to a particular key, the Lightstreamer server could optimize data transmission by sending only the fields from the JSON structure that have actually changed.


These scenarios demonstrate how key-based filtering and selective field transmission can enhance the scalability, efficiency, and responsiveness of data distribution in real-time streaming applications.

 __Key-based Filtering:__ By including a key value in each message sent to Kafka, Lightstreamer clients can subscribe to specific items associated with particular keys. This allows clients to receive only the messages relevant to their subscribed keys, reducing network bandwidth usage and processing overhead.

 __Efficient Message Processing:__ While a generic Kafka client would receive all messages and need to filter them based on the key, Lightstreamer clients can optimize message processing by subscribing directly to the desired key, resulting in more efficient message handling and reduced latency.

 __JSON Serialization with Key:__ Serializing JSON messages in Kafka with associated keys enables more granular data distribution and processing.

 __Selective Field Transmission:__ The Lightstreamer server can leverage the key-value pairs to optimize data transmission, sending only the fields from the JSON structure that have changed since the last update. This minimizes redundant data transfer and improves overall network efficiency.

 __Dynamic Data Updates:__ In real-world scenarios where JSON data structures contain numerous fields, transmitting only the changed fields allows for dynamic updates to be efficiently propagated to clients without unnecessary data overhead.

### Results

* __Scenario 1__ 

| N. Clients | 1K | 2K | 4K | 8K | 12K | 14K | 16K | 18K | 20K | 32K | 40K | 50K |
|----------|----------|----------|----------|----------|----------|----------|----------|----------|-----------|----------|-----------|-----------|
| Kafka Clients (N consumer groups) | 6 (17) | 13 (18) | 35 (344) | 53 (45) | 363 (1494) | 1068 (914) | 3376 (507) | x | x | x | x |
| Kafka Clients (standalone) | - | - | - | 74 (84) | - | 111 (59) | 375 (219) | 1202 (979) | 2201 (1944) |  x | x | x |
| Lightstreamer Clients | 10 (17) | 16 (15) | 27 (17) | 33 (21) | - | 52 (21) | 91 (37) | 144 (34) | 128 (147) | 158 (71) | 252 (87) | 787 (226) |



* __Scenario 2__ 

| N. Clients | 6K | 8K | 10K | 52K | 64K | 70K |
|----------|----------|----------|----------|----------|----------|----------|
| Kafka Clients (N consumer groups) | - | 1079 (221) | x | x | x |
| Kafka Clients (standalone) | 603 (695) | 756 (1097) | 1426 (4157) | x | x |
| Lightstreamer Clients | - | - | 22 (13) | 45 (37) | 98 (132) | 936 (1256) |

*Mean (Standard Deviation) expressed in millisecond*

## Usage

## Contributing
We welcome contributions from the community! If you encounter any issues, have feature requests, or would like to contribute enhancements to the benchmarking tool, please see the Contribution Guidelines for instructions on how to get involved.