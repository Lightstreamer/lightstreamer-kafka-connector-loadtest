#!/bin/bash

# Lightstreamer Kafka Connector Load Test Tools
# Build Script for All JAR Files
#
# This script builds all specialized JAR files for the Kafka connector tools:
# - jmh-benchmarks.jar (JMH performance benchmarks)
# - ls-consumer.jar (Lightstreamer Consumer)
# - message-generator.jar (Message Generator)

set -e  # Exit on any error

echo "=================================================="
echo "  Lightstreamer Kafka Connector - Build All JARs"
echo "=================================================="
echo

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "❌ Error: Maven (mvn) is not installed or not in PATH"
    echo "   Please install Maven first: https://maven.apache.org/install.html"
    exit 1
fi

echo "📂 Working directory: $PWD"
echo "🔨 Starting Maven build process..."
echo

# Clean and build all JARs
echo "🧹 Cleaning previous builds..."
mvn clean -q

echo "📦 Building all JAR files..."
mvn package -Pbuild-all -q

# Check if build was successful
if [ $? -eq 0 ]; then
    echo
    echo "✅ Build completed successfully!"
    echo
    echo "📋 Generated JAR files:"
    echo "├── target/jmh-benchmarks.jar"
    echo "├── target/ls-consumer.jar"
    echo "└── target/message-generator.jar"
    echo
    
    # Show file sizes
    echo "📊 JAR file sizes:"
    ls -lh target/*.jar | grep -E "(jmh-benchmarks|kafkaconnector-tools-)" | awk '{print "├── " $9 " (" $5 ")"}'
    echo
    
    echo "🚀 Usage examples:"
    echo "   JMH Benchmarks:"
    echo "   java -jar target/jmh-benchmarks.jar"
    echo
    echo "   Lightstreamer Consumer:"
    echo "   java -jar target/ls-consumer.jar [args...]"
    echo
    echo "   Message Generator:"
    echo "   java -jar target/message-generator.jar <bootstrap-servers> <topic> <num-producers> <pause-millis> <msg-size> <key-or-not>"
    echo
else
    echo
    echo "❌ Build failed!"
    echo "   Check the Maven output above for error details."
    exit 1
fi