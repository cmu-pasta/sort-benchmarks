package concurrency;

import cmu.pasta.sfuzz.overrides.Thread;
import cmu.pasta.sfuzz.schedules.ListSchedule;
import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(JQF.class)
public class VolatileTest {
    private volatile Integer x;

    @Fuzz @Ignore
    public void testVolatile(Integer input, @From(VolatileScheduleGenerator.class) ListSchedule schedule) throws InterruptedException {
        x = 0;
        Thread t1 = new Thread(() -> {
            int y = x;
            y += input;
            x = y;
        });
        Thread t2 = new Thread(() -> {
            int y = x;
            y -= input;
            x = y;
        });
        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        switch(((ListSchedule) schedule.deepCopy()).firstIndex()) {
            case 1:
                assertEquals(-1 * input, x.intValue());
                break;
            case 2:
                assertEquals(input, x);
                break;
            default:
                assertEquals(0, x.intValue());
                break;
        }
    }
}