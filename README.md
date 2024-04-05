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

*Key-based Filtering:* By including a key value in each message sent to Kafka, Lightstreamer clients can subscribe to specific items associated with particular keys. This allows clients to receive only the messages relevant to their subscribed keys, reducing network bandwidth usage and processing overhead.
*Efficient Message Processing:* While a generic Kafka client would receive all messages and need to filter them based on the key, Lightstreamer clients can optimize message processing by subscribing directly to the desired key, resulting in more efficient message handling and reduced latency.
*JSON Serialization with Key:* Serializing JSON messages in Kafka with associated keys enables more granular data distribution and processing.
*Selective Field Transmission:* The Lightstreamer server can leverage the key-value pairs to optimize data transmission, sending only the fields from the JSON structure that have changed since the last update. This minimizes redundant data transfer and improves overall network efficiency.
*Dynamic Data Updates:* In real-world scenarios where JSON data structures contain numerous fields, transmitting only the changed fields allows for dynamic updates to be efficiently propagated to clients without unnecessary data overhead.

## Usage
To use the benchmarking tool, follow the instructions provided in the Documentation section. Detailed guides and examples are available to help you set up and run benchmark tests against Lightstreamer and Kafka installations.

## Contributing
We welcome contributions from the community! If you encounter any issues, have feature requests, or would like to contribute enhancements to the benchmarking tool, please see the Contribution Guidelines for instructions on how to get involved.