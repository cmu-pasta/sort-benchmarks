package concurrency;

import cmu.pasta.cdiff.IndexedThread;
import cmu.pasta.cdiff.Schedule;
import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(JQF.class)
public class VolatileTest {
    private Integer x; //TODO shouldn't this cause the test to fail? (or, what would?)
    @Fuzz //@Ignore
    public void testVolatile(Integer input, @From(RandomScheduleGenerator.class) Schedule schedule) throws InterruptedException {
        IndexedThread t = new IndexedThread(() -> {
            x = 0;
            IndexedThread t1 = new IndexedThread(() -> {
                x += input;
            });
            IndexedThread t2 = new IndexedThread(() -> {
                x += input;
            });
            t1.start();
            t2.start();
            try {
                t1.newJoin();
                t2.newJoin();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            assertEquals(2 * input, x.intValue());
        });
        t.start();
    }
}