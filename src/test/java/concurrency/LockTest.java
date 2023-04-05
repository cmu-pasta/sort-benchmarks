package concurrency;

import cmu.pasta.cdiff.overrides.InstrumentedThread;
import cmu.pasta.cdiff.schedule.ListSchedule;
import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import java.util.HashMap;

import cmu.pasta.cdiff.overrides.InstrumentedReentrantLock;

import static org.junit.Assert.assertEquals;

@RunWith(JQF.class)
public class LockTest {
    private static final String KEY = "KEY";
    private HashMap<String, Integer> map;
    @Fuzz @Ignore
    public void testReentrantLock(Integer input, @From(ReentrantLockScheduleGenerator.class) ListSchedule schedule) throws InterruptedException {
        map = new HashMap<>();
        InstrumentedReentrantLock rl = new InstrumentedReentrantLock(); //TODO also deal with the lock's reentrantness (if can debug)
        InstrumentedThread t1 = new InstrumentedThread(() -> {
            System.out.println("starting t1");
            int val = input;
            System.out.println("t1 checking containsKey");
            rl.lock();
            System.out.println("t1 got lock");
            if(map.containsKey(KEY)) {
                rl.unlock();
                System.out.println("t1 contained key");

                rl.lock();
                val += map.get(KEY);
                rl.unlock();

                System.out.println("t1 got val " + val);
            } else {
                System.out.println("t1 did not contain key");
                rl.unlock();
                rl.lock();
                rl.unlock();
            }

            rl.lock();
            System.out.println("putting (" + KEY + ", " + val + ") in map");
            map.put(KEY, val);
            System.out.println("map: " + map);
            rl.unlock();
        });
        InstrumentedThread t2 = new InstrumentedThread(() -> {
            System.out.println("starting t2");
            int val = -1 * input;
            rl.lock();
            System.out.println("t2 checking containsKey");
            if(map.containsKey(KEY)) {
                rl.unlock();

                System.out.println("t2 contained key");

                rl.lock();
                val += map.get(KEY);
                rl.unlock();

                System.out.println("t2 got val " + val);
            } else {
                System.out.println("t2 did not contain key");
                rl.unlock();
                rl.lock();
                rl.unlock();
            }

            rl.lock();
            System.out.println("putting (" + KEY + ", " + val + ") in map");
            map.put(KEY, val);
            System.out.println("map: " + map);
            rl.unlock();
        });

        System.out.println("starting t1");
        t1.start();
        System.out.println("starting t2");
        t2.start();

        System.out.println("threads started");

        try {
            System.out.println("joining threads");
            t1.newJoin();
            t2.newJoin();
            System.out.println("threads joined");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("map: " + map);

        switch(((ListSchedule) schedule.deepCopy()).firstIndex()) {
            case 1:
                assertEquals(-1 * input, map.get(KEY).intValue());
                break;
            case 2:
                assertEquals(input, map.get(KEY));
                break;
            default:
                assertEquals(0, map.get(KEY).intValue());
                break;
        }
    }
}
