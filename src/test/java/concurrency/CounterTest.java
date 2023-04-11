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
public class CounterTest {
    public static class CounterScheduleGenerator extends Generator<ListSchedule> {
        public CounterScheduleGenerator() {
            super(ListSchedule.class);
        }

        public int num = 0;

        @Override
        public ListSchedule generate(SourceOfRandomness sourceOfRandomness, GenerationStatus generationStatus) {
            int seed = num++;
            switch (seed) {
                case 0: return new ListSchedule(List.of(0, 1, 0, 0, 1, 1, 0, 0, 0)); //start,t1,t1,t2,t2,main
                case 1: return new ListSchedule(List.of(0, 1, 1, 1, 0, 0, 0, 0, 0)); //start,t2,t2,t1,t1,main
                case 2: return new ListSchedule(List.of(1, 0, 0, 1, 0, 1, 0, 0, 0)); //start,t1,t2,t1,t2,main
                case 3: return new ListSchedule(List.of(2, 1, 1, 0, 1, 0, 0, 0, 0)); //start,t2,t1,t2,t1,main
                default: return new ListSchedule(List.of(0, 0, 0, 0, 0, 0, 0, 0, 0));
            }
        }
    }

    @Fuzz @Ignore
    public void testIncDec(String s, @From(CounterScheduleGenerator.class) ListSchedule schedule) {
        try(Scheduler scheduler = Scheduler.startWithSchedule(schedule)) {
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

            switch (((ListSchedule) schedule.deepCopy()).firstIndex()) {
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
}
