package concurrency;

import cmu.pasta.sfuzz.instrument.Scheduler;
import cmu.pasta.sfuzz.overrides.Thread;
import cmu.pasta.sfuzz.overrides.ReentrantLock;
import cmu.pasta.sfuzz.schedules.ListSchedule;
import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.List;


import static org.junit.Assert.assertEquals;

@RunWith(JQF.class)
public class LockTest {
    public static class LockScheduleGenerator extends Generator<ListSchedule> {
        public int num = 0;

        public LockScheduleGenerator() {
            super(ListSchedule.class);
        }
        @Override
        public ListSchedule generate(SourceOfRandomness sourceOfRandomness, GenerationStatus generationStatus) {
            int seed = num++;
            switch (seed) {
                //all starting with (start t1, start t2) = ([key], [not key])
                //all ending with (t1.join(),t2.join()) = (0, 0)
                case 0: return new ListSchedule(List.of(0,1, 0,0,0,0,0,0, 0,0)); //t1,t1,t1,t2,t2,t2
                case 1: return new ListSchedule(List.of(0,1, 1,1,1,0,0,0, 0,0)); //t2,t2,t2,t1,t1,t1
                case 2: return new ListSchedule(List.of(1,0, 0,0,1,0,0,0, 0,0)); //t1,t1,t2,t1,t2,t2
                case 3: return new ListSchedule(List.of(1,0, 0,0,1,1,0,0, 0,0)); //t1,t1,t2,t2,t1,t2
                case 4: return new ListSchedule(List.of(2,1, 0,0,1,1,1,0, 0,0)); //t1,t1,t2,t2,t2,t1
                case 5: return new ListSchedule(List.of(1,0, 0,1,0,0,1,0, 0,0)); //t1,t2,t1,t1,t2,t2
                case 6: return new ListSchedule(List.of(1,0, 0,1,0,1,0,0, 0,0)); //t1,t2,t1,t2,t1,t2
                case 7: return new ListSchedule(List.of(2,1, 0,1,0,1,1,0, 0,0)); //t1,t2,t1,t2,t2,t1
                case 8: return new ListSchedule(List.of(1,0, 0,1,1,0,0,0, 0,0)); //t1,t2,t2,t1,t1,t2
                case 9: return new ListSchedule(List.of(2,1, 0,1,1,0,1,0, 0,0)); //t1,t2,t2,t1,t2,t1
                case 10: return new ListSchedule(List.of(2,1, 0,1,1,1,0,0, 0,0)); //t1,t2,t2,t2,t1,t1
                case 11: return new ListSchedule(List.of(2,1, 1,1,0,1,0,0, 0,0)); //t2,t2,t1,t2,t1,t1
                case 12: return new ListSchedule(List.of(2,1, 1,1,0,0,1,0, 0,0)); //t2,t2,t1,t1,t2,t1
                case 13: return new ListSchedule(List.of(1,0, 1,1,0,0,0,0, 0,0)); //t2,t2,t1,t1,t1,t2
                case 14: return new ListSchedule(List.of(2,1, 1,0,1,1,0,0, 0,0)); //t2,t1,t2,t2,t1,t1
                case 15: return new ListSchedule(List.of(2,1, 1,0,1,0,1,0, 0,0)); //t2,t1,t2,t1,t2,t1
                case 16: return new ListSchedule(List.of(1,0, 1,0,1,0,0,0, 0,0)); //t2,t1,t2,t1,t1,t2
                case 17: return new ListSchedule(List.of(2,1, 1,0,0,1,1,0, 0,0)); //t2,t1,t1,t2,t2,t1
                case 18: return new ListSchedule(List.of(1,0, 1,0,0,1,0,0, 0,0)); //t2,t1,t1,t2,t1,t2
                case 19: return new ListSchedule(List.of(1,0, 1,0,0,0,0,0, 0,0)); //t2,t1,t1,t1,t2,t2
                default: return new ListSchedule(List.of(0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
            }
        }
    }

    private static final String KEY = "KEY";
    private HashMap<String, Integer> map;
    @Fuzz @Ignore
    public void testReentrantLock(Integer input, @From(LockScheduleGenerator.class) ListSchedule schedule) {
        try(Scheduler scheduler = Scheduler.startWithSchedule(schedule)) {
            map = new HashMap<>();
            ReentrantLock rl = new ReentrantLock();
            Thread t1 = new Thread(() -> {
                int val = input;
                rl.lock();
                if (map.containsKey(KEY)) {
                    rl.unlock();

                    rl.lock();
                    val += map.get(KEY);
                    rl.unlock();
                } else {
                    rl.unlock();
                    rl.lock();
                    rl.unlock();
                }

                rl.lock();
                map.put(KEY, val);
                rl.unlock();
            });
            Thread t2 = new Thread(() -> {
                int val = -1 * input;
                rl.lock();
                if (map.containsKey(KEY)) {
                    rl.unlock();

                    rl.lock();
                    val += map.get(KEY);
                    rl.unlock();
                } else {
                    rl.unlock();
                    rl.lock();
                    rl.unlock();
                }

                rl.lock();
                map.put(KEY, val);
                rl.unlock();
            });

            t1.start();
            t2.start();

            try {
                t1.newJoin();
                t2.newJoin();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            switch (((ListSchedule) schedule.deepCopy()).firstIndex()) {
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
}
