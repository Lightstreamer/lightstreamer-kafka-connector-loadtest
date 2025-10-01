#!/bin/bash

# JMH Benchmark Runner - Runs JMH benchmarks from src/jmh/java
# Run this script from the project root directory (where pom.xml is located)

echo "JMH Benchmark Suite"
echo "==================="
echo ""

# Build JMH benchmark jar
echo "Building JMH benchmark jar..."
mvn clean package -Pjmh -q

if [ $? -ne 0 ]; then
    echo "❌ JMH build failed!"
    exit 1
fi

echo "✅ JMH build successful!"
echo ""

# Check if a specific benchmark was requested
if [ "$1" ]; then
    echo "Running specific benchmark: $1"
    java -jar target/jmh-benchmarks.jar "$1"
else
    echo "Available benchmarks:"
    echo "1. PayloadBenchmark - Compare SimpleProducer vs ProtobufProducer payload generation"
    echo ""
    echo "Usage:"
    echo "  ./run-jmh-benchmark.sh                    # Run all benchmarks"
    echo "  ./run-jmh-benchmark.sh PayloadBenchmark   # Run specific benchmark"
    echo ""
    echo "Running ALL benchmarks..."
    java -jar target/jmh-benchmarks.jar
fi