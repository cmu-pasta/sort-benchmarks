package concurrency;

import cmu.pasta.cdiff.overrides.InstrumentedReentrantLock;
import cmu.pasta.cdiff.overrides.InstrumentedThread;
import cmu.pasta.cdiff.schedule.ListSchedule;
import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(JQF.class)
public class ReentrancyTest {
    private volatile int x;

    @Fuzz @Ignore
    public void testSynchronizedBlock(Integer input, @From(ReentrancyScheduleGenerator.class) ListSchedule schedule) throws InterruptedException {
        String KEY = "KEY";
        x = 0;

        InstrumentedThread t1 = new InstrumentedThread(() -> {
            int y;
            synchronized (KEY) {
                synchronized (KEY) {
                    y = x + input;
                }
            }
            synchronized (KEY) {
                synchronized (KEY) {
                    x = y;
                }
            }
        });
        InstrumentedThread t2 = new InstrumentedThread(() -> {
            int y;
            synchronized (KEY) {
                synchronized (KEY) {
                    y = x - input;
                }
            }
            synchronized (KEY) {
                synchronized (KEY) {
                    x = y;
                }
            }
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
                assertEquals(-1 * input, x);
                break;
            case 2:
                assertEquals(input.intValue(), x);
                break;
            default:
                assertEquals(0, x);
                break;
        }
    }

    @Fuzz @Ignore
    public void testReentrantLock(Integer input, @From(ReentrancyScheduleGenerator.class) ListSchedule schedule) throws InterruptedException {
        InstrumentedReentrantLock rl = new InstrumentedReentrantLock();
        x = 0;

        InstrumentedThread t1 = new InstrumentedThread(() -> {
            int y;
            rl.lock();
            rl.lock();
                    y = x + input;
            rl.unlock();
            rl.unlock();

            rl.lock();
            rl.lock();
            x = y;
            rl.unlock();
            rl.unlock();
        });
        InstrumentedThread t2 = new InstrumentedThread(() -> {
            int y;
            rl.lock();
            rl.lock();
            y = x - input;
            rl.unlock();
            rl.unlock();

            rl.lock();
            rl.lock();
            x = y;
            rl.unlock();
            rl.unlock();
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
                assertEquals(-1 * input, x);
                break;
            case 2:
                assertEquals(input.intValue(), x);
                break;
            default:
                assertEquals(0, x);
                break;
        }
    }
}
