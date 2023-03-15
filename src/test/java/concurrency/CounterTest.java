package concurrency;

import cmu.pasta.cdiff.ListSchedule;
import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(JQF.class)
public class CounterTest {
    @Fuzz @Ignore
    public void testIncDec(String s, @From(CounterScheduleGenerator.class) ListSchedule schedule) throws InterruptedException {
        Thread t = new Thread(() -> {
            //System.out.println("schedule: " + schedule + "(size > 5? " + (schedule.size() > 5) + ")");
            CounterMap cm = new CounterMap();

            Thread t1 = new Thread(() -> cm.putOrIncrement(s));
            Thread t2 = new Thread(() -> cm.putOrDecrement(s));
            t1.start();
            t2.start();
            try {
                //System.out.println("joining t1");
                t1.newJoin();
                //System.out.println("joining t2");
                t2.newJoin();
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
        });
        Map<String, Throwable> exceptions = new HashMap<>();
        t.setUncaughtExceptionHandler((t1, e) -> exceptions.put(t1 + "with:\nSchedule " + schedule + "\n", e));
        t.start();
        t.newJoin();
        for(Map.Entry<String, Throwable> entry : exceptions.entrySet()) {
            RuntimeException re = new RuntimeException(entry.getKey() + " threw " + entry.getValue());
            re.setStackTrace(entry.getValue().getStackTrace());
            throw re;
        }
    }
}
