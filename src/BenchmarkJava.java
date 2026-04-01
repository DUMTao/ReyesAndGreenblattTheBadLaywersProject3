import java.util.Random;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.lang.*;
/**
 * Runs a little benchmarking script in Java, and outputs the number of milliseconds.
 *
 * @author Kyle Burke
 */
public class BenchmarkJava {

    private static final int NUM_ROUNDS = 11;
    private static final long BENCHMARK_N = 110053500;

    public static void main(String[] args) {
        long result = benchmark(false);

        System.out.println(result);
    }

    public static long benchmark(boolean useNanos) {
        int n = NUM_ROUNDS;
        List<Long> a = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            long t = benchmarkOnce(useNanos);
            a.add(t);
        }
        Collections.sort(a);
        long median = a.get(a.size() / 2);
        //System.out.println("Median benchmark run: " + median);

        return median;
    }

    //run the benchmark once
    private static long benchmarkOnce(boolean useNanos) {
        long startMillis;
        if (useNanos) {
            startMillis = System.nanoTime() / 1000000;
        } else {
            startMillis = System.currentTimeMillis();
        }
        Random random = new Random();
        long total = 0;
        for (long i = 0; i < BENCHMARK_N; i++) {
            total += (i * random.nextInt());
        }
        long stopMillis;
        if (useNanos) {
            stopMillis = System.nanoTime() / 1000000;
        } else {
            stopMillis = System.currentTimeMillis();
        }
        long elapsed = stopMillis - startMillis;
        //System.out.println("The benchmark ran in " + elapsed + " milliseconds.");
        return elapsed;

    }



}  //end of BenchmarkJava