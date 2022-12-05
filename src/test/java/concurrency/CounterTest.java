package concurrency;

import cmu.pasta.cdiff.ListSchedule;
import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;

@RunWith(JQF.class)
public class CounterTest {
    @Fuzz
    public void testIncDec(String s, @From(CounterScheduleGenerator.class) ListSchedule schedule) throws InterruptedException {
        System.out.println("schedule: " + schedule + "(size > 5? " + (schedule.size() > 5) + ")");
        CounterMap cm = new CounterMap();

        concurrency.Thread t1 = new Thread(() -> cm.putOrIncrement(s));
        concurrency.Thread t2 = new Thread(() -> cm.putOrDecrement(s));
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("schedule: " + schedule + "(size > 5? " + (schedule.size() > 5) + ")");
        if(schedule.size() > 5) assertEquals(0, cm.getValue(s));
        else if(schedule.deepCopy().next() == 0) assertEquals(-1, cm.getValue(s));
        else assertEquals(1, cm.getValue(s));
    }
}
