import java.util.Random;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.lang.*;
import java.text.DecimalFormat;

/**
 * Code to test submitted solutions for balancing bike messenger saddlebags.
 *
 * @author Kyle Burke
 */
public class SpeedGrader {

    //whether we're using Java's internal nanosecond timers
    private static final boolean USE_NANOS = true;


    public SpeedGrader() {
        //no state
    }

    /**
     * Runs the tests and prints feedback if asserts are enabled.
     */
    public void runTests(CodeTester tester, double multiplier, int maxPoints, int[] sizes) {
        List<Integer> sizeList = new ArrayList<>();
        for (int i = 0; i < sizes.length; i++)  {
            sizeList.add(sizes[i]);
        }
        runTests(tester, multiplier, maxPoints, sizeList);
    }

    /**
     * Runs the tests and prints feedback if asserts are enabled.
     */
    public void runTests(CodeTester tester, double multiplier, int maxPoints, List<Integer> sizes) {
        //check that asserts are enforced
        boolean assertsEnabled = false;
        try {
            assert false;
        } catch (Throwable e) {
            assertsEnabled = true;
        }
        System.out.println("Are asserts enabled?  " + (assertsEnabled ? "Yes!" : "No!  You won't get the normal feedback."));

        //benchmark and set up for the tests
        tester.setUseNanos(USE_NANOS);
        long benchmarkMillis = BenchmarkJava.benchmark(USE_NANOS);
        System.out.println("Benchmark took: " + benchmarkMillis + " ms.");
        double allowedMillis = multiplier * benchmarkMillis;
        System.out.println("Each test will be capped at " + ((long) allowedMillis) + " ms.");
        Collections.sort(sizes); //make sure the sizes are in order!  (Just in case we messed up!)
        double score = 0.0;
        int numSizes = sizes.size();
        System.out.println("There are " + numSizes + " different problem sizes.");
        double pointsPerSize = ((double) maxPoints) / sizes.size();
        System.out.println("Each trial is worth " + pointsPerSize + " points.");
        DecimalFormat pointsFormat = new DecimalFormat("###.##");
        DecimalFormat sizeFormat = new DecimalFormat("###,###,###");

        //run the tests
        int numSuccesses = 0;
        long testTime = -1;
        for (int i = 0; i < sizes.size(); i++) {
            int problemSize = sizes.get(i);
            tester.setProblemSize(problemSize);
            try {
                testTime = tester.call();
            } catch (StackOverflowError e) {
                System.out.println("StackOverflow on size " + sizeFormat.format(problemSize) + "!");
                System.out.println("Remaining sizes: " + sizes.subList(i+1, sizes.size()));
                break;
            } catch (OutOfMemoryError e) {
                System.out.println("OutOfMemoryError on size " + sizeFormat.format(problemSize) + "!");
                System.out.println("Remaining sizes: " + sizes.subList(i+1, sizes.size()));
                break;
            } catch (Error e) {
                System.out.println("Error on size " + sizeFormat.format(problemSize) + ":");
                e.printStackTrace(System.out);
                throw new RuntimeException(e);
            }
            if (testTime < allowedMillis) {
                score += pointsPerSize;
                numSuccesses = i+1;
                if (numSuccesses == sizes.size()) {
                    //make the total a clean perfect score
                    score = (double) maxPoints;
                }
                System.out.println("Success on size " + sizeFormat.format(problemSize) + " in " + testTime + " ms.");
            } else {
                System.out.println("Timed out on size " + sizeFormat.format(problemSize) + ".  (Took " + testTime + " > " + allowedMillis + ".)");
                System.out.println("Remaining sizes: " + sizes.subList(i+1, sizes.size()));
                break;
            }
        }
        System.out.println("The last test ran in " + testTime + " ms.");

        if (assertsEnabled) {

            String teamMembers = tester.getCodeAuthors();

            System.out.println();
            System.out.println("*~~~~~~~ Summary for " + teamMembers + " ~~~~~~~~~~~*");
            System.out.println("Total score: " + pointsFormat.format(score) + "/" + maxPoints + "     (" + pointsFormat.format(pointsPerSize) + " x " + numSuccesses + ")");
            System.out.println();
            System.out.println("Reminder that this is only an official grade if Kyle ran it.  Your actual grade may be very different!  To get your official grade, please follow the submission instructions.");
        } else {
            System.out.println("You didn't enable asserts.  Please run again with them enabled to get summary feedback.");
        }

    }

    public static abstract class CodeTester implements Callable<Long> {

        protected int problemSize;

        protected boolean useNanos;

        public abstract Long call();

        public void setProblemSize(int problemSize) {
            this.problemSize = problemSize;
        }

        public abstract String getCodeAuthors();

        public void setUseNanos(boolean useNanos) {
            this.useNanos = useNanos;
        }

        protected long getSystemMillis() {
            if (this.useNanos) {
                return System.nanoTime() / 1000000;
            } else {
                return System.currentTimeMillis();
            }
        }

    }
}