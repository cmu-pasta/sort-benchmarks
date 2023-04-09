package concurrency;

import cmu.pasta.sfuzz.overrides.ReentrantLock;
import cmu.pasta.sfuzz.overrides.Thread;
import cmu.pasta.sfuzz.schedules.ListSchedule;
import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(JQF.class)
public class ReentrancyTest {

    public static class ReentrancyScheduleGenerator extends Generator<ListSchedule> {
        public int num = 0;

        public ReentrancyScheduleGenerator() {
            super(ListSchedule.class);
        }
        @Override
        public ListSchedule generate(SourceOfRandomness sourceOfRandomness, GenerationStatus generationStatus) {
            int seed = num++;
            switch (seed) {
                //all starting with (x=1, start t1, start t2) = ([key], 0, 1)
                //all ending with (t1.join(),t2.join(),load x) = (0, 0, 0)
                case 0: return new ListSchedule(List.of(0,0,1, 0,0,0,0,0,0,0,0,0,0,0,0, 0,0,0)); //t1,t1,t1,t1,t1,t1,t2,t2,t2,t2,t2,t2
                case 1: return new ListSchedule(List.of(0,0,1, 1,1,1,1,1,1,0,0,0,0,0,0, 0,0,0)); //t2,t2,t2,t2,t2,t2,t1,t1,t1,t1,t1,t1
                case 2: return new ListSchedule(List.of(1,0,1, 0,0,0,1,1,1,0,0,0,0,0,0, 0,0,0)); //t1,t1,t1,t2,t2,t2,t1,t1,t1,t2,t2,t2
                case 3: return new ListSchedule(List.of(2,0,1, 1,1,1,0,0,0,1,1,1,0,0,0, 0,0,0)); //t2,t2,t2,t1,t1,t1,t2,t2,t2,t1,t1,t1
                default: return new ListSchedule(List.of(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
            }
        }
    }

    private volatile int x;

    @Fuzz @Ignore
    public void testSynchronizedBlock(Integer input, @From(ReentrancyScheduleGenerator.class) ListSchedule schedule) throws InterruptedException {
        //TODO could use the try-with-schedule here instead of in guidance
        String KEY = "KEY";
        x = 0;

        Thread t1 = new Thread(() -> {
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
        Thread t2 = new Thread(() -> {
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
        System.out.println("my schedule is " + schedule);
        ReentrantLock rl = new ReentrantLock();
        x = 0;

        Thread t1 = new Thread(() -> {
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
        Thread t2 = new Thread(() -> {
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
