# Build Instructions

This document describes how to build the Lightstreamer Kafka Connector Load Test Tools.

## Quick Start

### Using Build Script (Recommended)

```bash
./build.sh
```

This script will build all JAR files and provide usage instructions.

## Manual Build

### Prerequisites

- Java 17 or higher
- Apache Maven 3.6 or higher

### Build All JARs

```bash
mvn clean package -Pbuild-all
```

### Build Individual JARs

```bash
# JMH Benchmarks
mvn clean package -Pjmh

# Lightstreamer Consumer
mvn clean package -Pls-consumer

# Message Generator
mvn clean package -Pmessage-generator
```

## Generated JAR Files

After building, you'll find these JAR files in the `target/` directory:

| JAR File | Main Class | Purpose |
|----------|------------|---------|
| `jmh-benchmarks.jar` | `org.openjdk.jmh.Main` | JMH performance benchmarks |
| `kafkaconnector-tools-ls-consumer.jar` | `com.lightstreamer.LightstreamerConsumer` | Lightstreamer Consumer |
| `kafkaconnector-tools-message-generator.jar` | `com.lightstreamer.MessageGenerator` | Message Generator |

## Usage Examples

### JMH Benchmarks
```bash
java -jar target/jmh-benchmarks.jar
```

### Lightstreamer Consumer
```bash
java -jar target/kafkaconnector-tools-ls-consumer.jar [arguments...]
```

### Message Generator
```bash
java -jar target/kafkaconnector-tools-message-generator.jar \
  <bootstrap-servers> <topic> <num-producers> <pause-millis> <msg-size> <key-or-not>
```

Example:
```bash
java -jar target/kafkaconnector-tools-message-generator.jar \
  localhost:9092 test-topic 1 1000 1024 key
```

## Maven Profiles

- **`build-all`**: Builds all JAR files in one command
- **`jmh`**: Builds only the JMH benchmarks JAR
- **`ls-consumer`**: Builds only the Lightstreamer Consumer JAR  
- **`message-generator`**: Builds only the Message Generator JAR

## Development Notes

- All JARs are self-contained with dependencies included
- JMH benchmarks include src/jmh/java sources automatically
- Protocol Buffers definitions are compiled automatically during build
- Build script includes error checking and helpful output formatting