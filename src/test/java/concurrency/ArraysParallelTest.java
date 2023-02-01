package concurrency;

import cmu.pasta.cdiff.Schedule;
import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.generator.Size;
import concurrency.sort.ArrayPrefixHelpers;
import concurrency.sort.DualPivotQuicksort;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import java.util.*;
import java.util.concurrent.ForkJoinPool;

import static org.junit.Assert.assertEquals;

/** tests parallel methods of java.util.Arrays for ints and Integer objects */
@RunWith(JQF.class)
public class ArraysParallelTest {
    protected static final int MAX_SIZE = 160;
    protected static final int MIN_ELEMENT = 0;
    protected static final int MAX_ELEMENT = 10;

    @Fuzz @Ignore
    public void testSort(@Size(max=MAX_SIZE) List<@InRange(minInt=MIN_ELEMENT, maxInt=MAX_ELEMENT) Integer> input, @From(RandomScheduleGenerator.class) Schedule s) {
        //Integer[] arr = input.toArray(new Integer[0]);
        //Arrays.parallelSort(arr, Integer::compareTo);
        int[] arr = new int[input.size()];
        for(int c = 0; c < arr.length; c++) arr[c] = input.get(c);
        DualPivotQuicksort.sort(arr, ForkJoinPool.getCommonPoolParallelism(), 0, arr.length);
        input.sort(Integer::compareTo);
        for(int c = 0; c < arr.length; c++) {
            assertEquals(input.get(c).intValue(), arr[c]);
        }
    }

    //@Fuzz
    public void testPrefix(@Size(max=MAX_SIZE) List<@InRange(minInt=MIN_ELEMENT, maxInt=MAX_ELEMENT) Integer> input) {
        Integer[] arr = input.toArray(new Integer[0]);
        //Arrays.parallelPrefix(arr, (int1, int2) -> int1 + 1);
        if (arr.length > 0)
            new ArrayPrefixHelpers.CumulateTask<>
                    (null, (int1, int2) -> int1 + 1, arr, 0, arr.length).invoke();
        int init = input.get(0);
        for(int c = 0; c < arr.length; c++) {
            assertEquals(init + c, arr[c].intValue());
        }
    }

    //note that Arrays.parallelSetAll(arr, value -> value + 1); is equivalent to IntStream.range(0, arr.length).parallel().forEach(i -> { arr[i] = i + 1; });
    //which is harder to test, so not included here
}
