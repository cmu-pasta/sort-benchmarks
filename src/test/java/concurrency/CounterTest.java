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
public class CounterTest {
    @Fuzz @Ignore
    public void testIncDec(String s, @From(CounterScheduleGenerator.class) ListSchedule schedule) throws InterruptedException {
        //System.out.println("schedule: " + schedule + " (first val " + ((ListSchedule) schedule.deepCopy()).firstIndex() + ")");
        CounterMap cm = new CounterMap();

        Thread t1 = new Thread(() -> cm.putOrIncrement(s));
        Thread t2 = new Thread(() -> cm.putOrDecrement(s));
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
                assertEquals(-1, cm.getValue(s));
                break;
            case 2:
                assertEquals(1, cm.getValue(s));
                break;
            default:
                assertEquals(0, cm.getValue(s));
                break;
        }
    }
}
