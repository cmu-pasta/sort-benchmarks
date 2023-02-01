package concurrency;

import cmu.pasta.cdiff.Schedule;
import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.generator.Size;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(JQF.class)
public class ParallelSortTest {
    protected static final int MAX_SIZE = 8;
    protected static final int MIN_ELEMENT = 0;
    protected static final int MAX_ELEMENT = 10;

    @Fuzz @Ignore
    public void testParallelMergeSort(@Size(max=MAX_SIZE) List<@InRange(minInt=MIN_ELEMENT, maxInt=MAX_ELEMENT) Integer> input, @From(RandomScheduleGenerator.class) Schedule s) {
        System.out.println("sorting " + input);
        Integer[] parallelSorted = new BadParallelMergeSort().sort(input.toArray(new Integer[]{}));
        input.sort(Integer::compareTo);
        for(int c = 0; c < input.size(); c++) {
            assert(input.get(c).intValue() == parallelSorted[c].intValue());
        }
    }
}
