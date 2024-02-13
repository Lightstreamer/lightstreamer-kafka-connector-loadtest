package com.lightstreamer;

import java.text.DecimalFormat;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StatisticsManager {

    private Logger latencyLogger = LogManager.getLogger(StatisticsManager.class);

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1,
            new ThreadFactory() {
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "Latency report thread");
                    return t;
                }
            });

    // if we keep 1 long (8 bytes) per each millis of delay: 60*1000*8 = 480000 b =
    // 468 Kb = ~0.5 Mb
    // the index of the array is the delay in millis while the value is the number
    // of updates with such delay
    private long[] delays;
    // sum of all the delays, divided by valuesCount will give the average delay
    private long sum = 0;
    // cumulative number of updates received
    private long valuesCount = 0;
    // values of percentiles to be printed
    private static int[] percValues = { 25, 50, 75, 90, 95, 98 };
    private static int ninetyIndex = 3; // we need that 90 is available in the percValues list and that this var
                                        // contains its index to print the zoomed graph
    // max delay ever
    private int maxDelayFound = Integer.MIN_VALUE;
    // min delay ever
    private int minDelayFound = Integer.MAX_VALUE;
    // string buffer used to compone log report
    StringBuffer results = new StringBuffer();

    private ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<Integer>();

    private static DecimalFormat graphLabelsFormatter = new DecimalFormat("0000");

    public static final int MAX_LATENCY_MILLIS = 240000;
    public static final int LATENCY_GRAPH_COLUMNS = 8;
    public static final int LATENCY_REPORT_INTERVAL_MILLIS = 60000;

    private long startMillis;

    public StatisticsManager() {

        latencyLogger.info("The latency for each update is calculated as the difference between the " +
                "timestamp of when the update is processed by the Client Simulator and the timestamp of when " +
                "the update is generated by the Adapter Simulator.\nThe latency reporting system automatically " +
                "synchronizes the application clocks of the Adapter Simulator and the Client Simulator, while " +
                "removing any network latencies. The best practice is to run an instance of the Client Simulator " +
                "with a small number of sessions (perhaps on the same machine of Lightstreamer Server) to sample " +
                "latencies.\nThe real massive traffic will be generated by other instances of the Client Simulator " +
                "running on different machines, which do not analyze latencies.");

        this.startMillis = TimeConversion.getTimeMillis();

        // in case delays > conf.maxDelayMillis are received then the code will fail and
        // conf.maxDelayMillis have to be changed
        this.delays = new long[MAX_LATENCY_MILLIS];
        // init the delays array
        for (int i = 0; i < this.delays.length; i++) {
            this.delays[i] = 0;
        }

        new DequeueThread().start();

        // schedule the report to be printed each second
        scheduledThreadPoolExecutor.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                generateReport();
            }
        }, LATENCY_REPORT_INTERVAL_MILLIS, LATENCY_REPORT_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
    }

    public synchronized void generateReport() {
        latencyLogger.info("Generating latency report...");
        if (valuesCount == 0) {
            latencyLogger.info("No updates received to generate report");
            return;
        } else if (maxDelayFound == 0) {
            latencyLogger.info("all the " + valuesCount + " received updates have a <1ms delay");
            return;
        }

        long startGenerationTime = TimeConversion.getTimeMillis();

        if (latencyLogger.isDebugEnabled()) {
            // Beware that, unlike in most collections, this method is NOT a constant-time
            // operation.
            // Because of the asynchronous nature of these queues, determining the current
            // number of elements requires an O(n) traversal.
            latencyLogger.debug("Data that will not be processed: " + queue.size());
        }

        results.setLength(0);
        results.append("\n\n\nTest Duration: ");
        appendElapsed(startGenerationTime, results);

        // AVG-RELATED
        // calculate the average delay
        long avg = sum / valuesCount; // Math.round((double) sum/valuesCount);

        // PERCENTILE-RELATED
        // this array will contain the results of the percentile entries
        long[] percResults = new long[percValues.length];
        // this array will contain a value per each entry in the percValues array.
        // such value is the number of updates to be processed before generating the
        // percResults for the correspondent value in percValues
        long[] percSignals = new long[percValues.length];
        for (int i = 0; i < percSignals.length; i++) {
            percSignals[i] = (long) Math.ceil(((double) valuesCount / 100) * percValues[i]);

            /*
             * results.append("\n");
             * results.append(percValues[i]);
             * results.append("% - ");
             * results.append(percSignals[i]);
             * results.append(" samples");
             */
        }
        // the index of the percentile to be calculated next
        int percIndex = 0;
        // the number of processed updates
        int processed = 0;

        // GRAPH-RELATED
        // we'll prepare a distribution graph made of conf.graphSteps steps
        // graphStep will be the range size of each step
        long graphStep = (long) Math.ceil((double) (maxDelayFound - minDelayFound) / LATENCY_GRAPH_COLUMNS);
        graphStep = graphStep > 0 ? graphStep : 1;
        // this array will represent, per each step, the max value
        long[] maxValueForSteps = new long[LATENCY_GRAPH_COLUMNS];
        // will represent the real number of updates in each step
        long[] stepsCount = new long[LATENCY_GRAPH_COLUMNS];
        // init maxValueForSteps and stepsCount
        for (int i = 0; i < LATENCY_GRAPH_COLUMNS; i++) {
            maxValueForSteps[i] = ((i + 1) * graphStep) + minDelayFound;
            stepsCount[i] = 0;
        }
        // the step counting now
        int runningStep = 0;

        // STANDARD-DEVIATION RELATED
        // sum needed to calculate the standard deviation: sqrt {[ (value1-avg)^2 +
        // (value2-avg)^2 + (value3-avg)^2 ... (valueN-avg)^2] / N }
        long deviationSum = 0;

        for (int i = minDelayFound; i <= maxDelayFound; i++) {
            if (this.delays[i] > 0) {

                // STANDARD-DEVIATION RELATED
                deviationSum += (Math.pow(i - avg, 2) * this.delays[i]);

                // PERCENTILE-RELATED
                processed += this.delays[i];
                while (percIndex < percSignals.length && processed >= percSignals[percIndex]) {
                    // the maximum delay for the percValues[percIndex]% of the updates is i
                    percResults[percIndex] = i;
                    // go on with the next percentile
                    percIndex++;
                }

                // GRAPH-RELATED
                // if i (the delay) is bigger than delay of the current step we pass to the
                // following step
                while (i > maxValueForSteps[runningStep]) {
                    runningStep++;
                }
                // count how many updates fall in this step
                stepsCount[runningStep] += this.delays[i];

            }
        }

        // ZOOMED-GRAPH-RELATED
        // we'll prepare a second distribution graph made of conf.graphSteps steps, this
        // time the max value will be percResults[percResults.length-1]
        // graphStep will be the range size of each step
        long graphStepP = (long) Math
                .ceil((double) (percResults[ninetyIndex] - minDelayFound) / LATENCY_GRAPH_COLUMNS);
        graphStepP = graphStepP > 0 ? graphStepP : 1;
        // this array will represent, per each step, the max value
        long[] maxValueForStepsP = new long[LATENCY_GRAPH_COLUMNS];
        // will represent the real number of updates in each step
        long[] stepsCountP = new long[LATENCY_GRAPH_COLUMNS];
        // init maxValueForSteps and stepsCount
        for (int i = 0; i < LATENCY_GRAPH_COLUMNS; i++) {
            maxValueForStepsP[i] = ((i + 1) * graphStepP) + minDelayFound;
            stepsCountP[i] = 0;
        }
        // the step counting now
        int runningStepP = 0;
        for (int i = minDelayFound; i <= percResults[ninetyIndex]; i++) {
            if (this.delays[i] > 0) {
                // GRAPH-RELATED
                // if i (the delay) is bigger than delay of the current step we pass to the
                // following step
                while (i > maxValueForStepsP[runningStepP]) {
                    runningStepP++;
                }
                // count how many updates fall in this step
                stepsCountP[runningStepP] += this.delays[i];
            }
        }

        // calculate the standard deviation
        long deviation = Math.round(Math.sqrt((double) deviationSum / valuesCount));

        // show the results!

        results.append("\nNumber of samples: ");
        results.append(valuesCount);

        results.append("\nMin = ");
        results.append(minDelayFound);
        results.append(" ms");

        results.append("\nMean = ");
        results.append(avg);
        results.append(" ms - Standard Deviation = ");
        results.append(deviation);
        results.append(" ms");

        results.append("\nMax = ");
        results.append(maxDelayFound);
        results.append(" ms");

        for (int i = 0; i < percResults.length; i++) {
            results.append("\n");
            results.append(percValues[i]);
            results.append("th Percentile = ");
            results.append(percResults[i]);
            results.append(" ms");
        }

        // print the graph
        appendGraph(graphStep, stepsCount, maxValueForSteps, maxDelayFound, "Max", results);

        results.append("\n\nZOOM");
        // print the zoomed graph
        appendGraph(graphStepP, stepsCountP, maxValueForStepsP, percResults[ninetyIndex], "90th percentile", results);

        latencyLogger.info(results);

        if (latencyLogger.isDebugEnabled()) {
            long endGenerationTime = TimeConversion.getTimeMillis();
            latencyLogger.debug("Report generated in " + (endGenerationTime - startGenerationTime) + " ms");
        }
    }

    private void appendElapsed(long startGenerationTime, StringBuffer result) {

        long elapsedSec = (startGenerationTime - this.startMillis) / 1000;
        if (elapsedSec >= 3600) {
            // hours minutes and seconds
            long elapsedHours = elapsedSec / 3600;
            elapsedSec = elapsedSec % 3600;
            result.append(elapsedHours);

        } else {
            result.append(0);
        }
        result.append(" Hours ");

        if (elapsedSec >= 60) {
            // minutes and seconds
            long elapsedMin = elapsedSec / 60;
            elapsedSec = elapsedSec % 60;
            result.append(elapsedMin);
        } else {
            result.append(0);
        }
        result.append(" minutes and ");

        result.append(elapsedSec);
        result.append(" seconds");

    }

    private static void appendGraph(long graphStep, long[] values, long[] maxValueForSteps, long maxDelayFound,
            String maxLabel, StringBuffer results) {
        /*
         * Min
         * .*
         * .***
         * .*****
         * .********
         * .********************
         * .******
         * .
         * .*
         * Max
         */

        // max and min values in one of the step. 0 is not a valid value for min
        // (0 is not relevant to print the graph as it is represented by 0 asterisks)
        long min = 0;
        long max = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];
            }
            if (values[i] < min || min == 0) {
                min = values[i];
            }
        }

        // the maximum number of asterisk we want for a single step, if
        // we have to force this value the graph will be distorted
        int maxShown = 20;

        double div = min;
        if ((double) max / div > maxShown) {
            // the graph will be distorted to represent min and max without exceding in the
            // number of used asterisks
            div = (double) max / maxShown;
        }
        div = div < 1 ? 1 : div;

        /*
         * results.append("\nstep range: ");
         * results.append(graphStep);
         * results.append(" ms");
         * results.append("\n* ~= ");
         * results.append(div);
         * results.append(" samples");
         */

        results.append("\nMin");

        for (int i = 0; i < values.length; i++) {
            if (maxValueForSteps[i] - graphStep > maxDelayFound) {
                break;
            }

            results.append("\n");
            results.append(graphLabelsFormatter.format(maxValueForSteps[i] - graphStep));
            results.append(" - ");
            results.append(graphLabelsFormatter.format(maxValueForSteps[i]));
            results.append(" ");

            long num = 0;
            if (values[i] > 0) {
                num = Math.round(values[i] / div);
            }
            for (int x = 1; x <= num; x++) {
                results.append("*");
            }
            if (values[i] > 0) {
                results.append("|");
            }
        }

        results.append("\n");
        results.append(maxLabel);

    }

    public void onData(int delay) {
        queue.add(delay);
    }

    private void dequeueData() {
        while (true) {
            while (queue.isEmpty()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }

            int delay = queue.poll();

            // if delay < 0 or delay conf.maxDelayMillis we'll have an OutOfBoundException
            try {
                synchronized (this) {
                    this.delays[delay]++;
                    sum += delay;
                    valuesCount++;
                    if (maxDelayFound < delay) {
                        maxDelayFound = delay;
                    }
                    if (minDelayFound > delay) {
                        minDelayFound = delay;
                    }
                }

            } catch (ArrayIndexOutOfBoundsException ex) {
                if (delay < 0) {
                    latencyLogger.error(
                            "Negative delay received; the network latency during the test was probably smaller than the network latency detected when synchronizing clocks",
                            ex);
                } else {
                    latencyLogger.error("A delay > " + MAX_LATENCY_MILLIS
                            + " was received. Please change the Constants.MAX_DELAY_MILLIS constant to support such big delays",
                            ex);
                }
            }
        }
    }

    private class DequeueThread extends Thread {
        public void run() {
            dequeueData();
        }
    }

}
