package concurrency;

import cmu.pasta.sfuzz.instrument.Scheduler;
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
public class VolatileTest {
    public static class VolatileScheduleGenerator extends Generator<ListSchedule> {
        public int num = 0;

        public VolatileScheduleGenerator() {
            super(ListSchedule.class);
        }
        @Override
        public ListSchedule generate(SourceOfRandomness sourceOfRandomness, GenerationStatus generationStatus) {
            int seed = num++;
            switch (seed) {
                //all starting with (x=0, start t1, start t2) = ([key], 0, 1)
                //all ending with (t1.join(),t2.join(),x.intValue()) = (0, 0, 0)
                case 0: return new ListSchedule(List.of(0,0,1, 0,0,0,0, 0,0,0)); //t1,t1,t2,t2
                case 1: return new ListSchedule(List.of(0,0,1, 1,1,0,0, 0,0,0)); //t2,t2,t1,t1
                case 2: return new ListSchedule(List.of(1,0,1, 0,1,0,0, 0,0,0)); //t1,t2,t1,t2
                case 3: return new ListSchedule(List.of(2,0,1, 1,0,1,0, 0,0,0)); //t2,t1,t2,t1
                default: return new ListSchedule(List.of(0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
            }
        }
    }

    private volatile Integer x;

    @Fuzz @Ignore
    public void testVolatile(Integer input, @From(VolatileScheduleGenerator.class) ListSchedule schedule) {
        try(Scheduler scheduler = Scheduler.startWithSchedule(schedule)) {
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
            switch (((ListSchedule) schedule.deepCopy()).firstIndex()) {
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
}