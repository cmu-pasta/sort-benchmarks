package concurrency;

import cmu.pasta.cdiff.IndexedThread;
import cmu.pasta.cdiff.Schedule;
import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.generator.Size;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import java.util.*;

import static org.junit.Assert.assertEquals;

@RunWith(JQF.class)
public class ParallelSortTest {
    protected static final int MAX_SIZE = 8;
    protected static final int MIN_ELEMENT = 0;
    protected static final int MAX_ELEMENT = 10;

    @Fuzz //@Ignore
    public void testBadParallelMergeSort(@Size(max=MAX_SIZE) List<@InRange(minInt=MIN_ELEMENT, maxInt=MAX_ELEMENT) Integer> input, @From(RandomScheduleGenerator.class) Schedule s) throws InterruptedException {
        IndexedThread t = new IndexedThread(() -> {
            //System.out.println("sorting " + input + " with schedule " + s);
            Integer[] parallelSorted = new BadParallelMergeSort().sort(input.toArray(new Integer[]{}));
            List<Integer> other = new ArrayList<>(input);
            other.sort(Integer::compareTo);
            for (int c = 0; c < other.size(); c++) {
                assertEquals(other.get(c), parallelSorted[c]);
            }
            //System.out.println("got " + Arrays.toString(parallelSorted));
        });
        t.start();
    }

    @Fuzz //@Ignore
    public void testGoodParallelMergeSort(@Size(max=MAX_SIZE) List<@InRange(minInt=MIN_ELEMENT, maxInt=MAX_ELEMENT) Integer> input, @From(RandomScheduleGenerator.class) Schedule s) throws InterruptedException {
        IndexedThread t = new IndexedThread(() -> {
            //System.out.println("sorting " + input + " with schedule " + s);
            Integer[] parallelSorted = new ParallelMergeSort().sort(input.toArray(new Integer[]{}));
            List<Integer> other = new ArrayList<>(input);
            other.sort(Integer::compareTo);
            for (int c = 0; c < other.size(); c++) {
                assertEquals(other.get(c), parallelSorted[c]);
            }
            //System.out.println("got " + Arrays.toString(parallelSorted));
        });
        t.start();
    }
}
