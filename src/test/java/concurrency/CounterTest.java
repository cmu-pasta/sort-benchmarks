package concurrency;

import cmu.pasta.cdiff.overrides.InstrumentedThread;
import cmu.pasta.cdiff.schedule.ListSchedule;
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
        //TODO boil the test back to original, uncomplicate as much as possible
        //System.out.println("schedule: " + schedule + " (first val " + ((ListSchedule) schedule.deepCopy()).firstIndex() + ")");
        CounterMap cm = new CounterMap();

        InstrumentedThread t1 = new InstrumentedThread(() -> cm.putOrIncrement(s));
        InstrumentedThread t2 = new InstrumentedThread(() -> cm.putOrDecrement(s));
        t1.start();
        t2.start();
        try {
            //System.out.println("joining t1");
            t1.join();
            //System.out.println("joining t2");
            t2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //System.out.println("schedule: " + schedule + "(size > 5? " + (schedule.size() > 5) + ")");
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
