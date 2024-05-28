/*
 * Copyright (C) 2024 Lightstreamer Srl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.lightstreamer;

import java.util.Arrays;
import org.apache.commons.math3.special.Erf;

public class StatisticsCalculator {
    private long[] values;
    private int count;

    public StatisticsCalculator(int size) {
        values = new long[size];
        count = 0;
    }

    public void addValue(long value) {
        if (count < values.length) {
            values[count] = value;
            count++;
        } else {
            long[] nuovoArray = Arrays.copyOf(values, values.length + 1);
            nuovoArray[nuovoArray.length - 1] = value;
            values = nuovoArray;
        }
    }

    public double calculateMean() {
        if (count == 0) {
            return 0;
        }

        double sum = 0;
        for (int i = 0; i < count; i++) {
            sum += values[i];
        }

        return sum / count;
    }

    public double calculateMedian() {
        if (count == 0) {
            return 0;
        }

        // Sort the array
        java.util.Arrays.sort(values, 0, count);

        // Calculate median
        if (count % 2 == 0) {
            // Even number of elements, average middle two
            int middle = count / 2;
            double median = (values[middle - 1] + values[middle]) / 2.0;
            return median;
        } else {
            // Odd number of elements, return middle one
            int middle = count / 2;
            return values[middle];
        }
    }

    public double calculateConfidenceInterval(double zValue) {
        if (count == 0) {
            return 0;
        }

        double standardDeviation = calculateStandardDeviation();
        double marginOfError = zValue * (standardDeviation / Math.sqrt(count));
        return marginOfError;
    }

    private double calculateStandardDeviation() {
        double mean = calculateMean();
        double sumSquaredDiff = 0;

        for (int i = 0; i < count; i++) {
            double diff = values[i] - mean;
            sumSquaredDiff += diff * diff;
        }

        return Math.sqrt(sumSquaredDiff / count);
    }

    public static void main(String[] args) {
        StatisticsCalculator calculator = new StatisticsCalculator(5);

        calculator.addValue(10);
        calculator.addValue(15);
        calculator.addValue(12);
        calculator.addValue(18);
        calculator.addValue(25);

        double mean = calculator.calculateMean();
        double median = calculator.calculateMedian();
        double confidenceInterval = calculator.calculateConfidenceInterval(ZInverseErf.zInverseErf(0.95));

        System.out.println("Mean: " + mean);
        System.out.println("Median: " + median);
        System.out.println("Confidence Interval: " + confidenceInterval);
    }
}

class ZInverseErf {
    // Inverse Error Function using Commons Math library
    public static double zInverseErf(double probability) {
        return Erf.erfInv(probability);
    }
}