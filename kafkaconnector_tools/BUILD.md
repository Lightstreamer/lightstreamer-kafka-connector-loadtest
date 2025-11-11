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

# Kafka Consumer
mvn clean package -Pconsumer
```

## Generated JAR Files

After building, you'll find these JAR files in the `target/` directory:

| JAR File | Main Class | Purpose |
|----------|------------|---------|
| `jmh-benchmarks.jar` | `org.openjdk.jmh.Main` | JMH performance benchmarks |
| `ls-consumer.jar` | `com.lightstreamer.LightstreamerConsumer` | Lightstreamer Consumer |
| `message-generator.jar` | `com.lightstreamer.MessageGenerator` | Message Generator |
| `consumer.jar` | `com.lightstreamer.Main` | Kafka Consumer |

## Usage Examples

### JMH Benchmarks
```bash
java -jar target/jmh-benchmarks.jar
```

### Lightstreamer Consumer
```bash
# Named parameters (recommended)
java -jar target/ls-consumer.jar --server <server-url> --from-key <start> --to-key <end>

# Positional parameters (legacy compatibility)
java -jar target/ls-consumer.jar <server-address> <from-key> <to-key>
```

Examples:
```bash
# Show usage help
java -jar target/ls-consumer.jar --help

# Named parameters with short options
java -jar target/ls-consumer.jar -s http://localhost:8080 -f 0 -t 99 --verbose

# Named parameters with long options
java -jar target/ls-consumer.jar --server http://localhost:8080 --from-key 0 --to-key 99

# Legacy positional parameters (backward compatible)
java -jar target/ls-consumer.jar http://localhost:8080 0 99

# Using the helper script
./ls-consumer.sh 0 99
```

### Message Generator
```bash
java -jar target/message-generator.jar \
  <bootstrap-servers> <topic> <num-producers> <pause-millis> <msg-size> <key-or-not>
```

### Kafka Consumer
```bash
java -jar target/consumer.jar \
  <bootstrap-servers> <topic> <consumers> <consumer-group-id> <boolean-flag>
```

Example:
```bash
java -jar target/consumer.jar \
  localhost:9092 test-topic 1 my-group true
```

Or use the helper script:
```bash
./run-consumer.sh
./run-consumer.sh localhost:9092 test-topic 1 my-group true
```

## Maven Profiles

- **`build-all`**: Builds all JAR files in one command
- **`jmh`**: Builds only the JMH benchmarks JAR
- **`ls-consumer`**: Builds only the Lightstreamer Consumer JAR  
- **`message-generator`**: Builds only the Message Generator JAR
- **`consumer`**: Builds only the Kafka Consumer JAR

## Development Notes

- All JARs are self-contained with dependencies included
- JMH benchmarks include src/jmh/java sources automatically
- Protocol Buffers definitions are compiled automatically during build
- Build script includes error checking and helpful output formatting