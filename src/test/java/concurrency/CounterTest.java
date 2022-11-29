package concurrency;

import cmu.pasta.cdiff.Schedule;
import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.generator.InRange;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(JQF.class)
public class CounterTest {
    //TODO see PatriciaTrie

    protected static final int MIN_ELEMENT = 0;
    protected static final int MAX_ELEMENT = 10;
    protected static final String DEFAULT_KEY = "key_string";

    @Fuzz
    public void testIncDec(String s, @From(ScheduleGenerator.class) Schedule schedule) throws InterruptedException {
        System.out.println("starting testIncDec");
        CounterMap cm = new CounterMap();

        concurrency.Thread t1 = new Thread(() -> cm.putOrIncrement(s));
        concurrency.Thread t2 = new Thread(() -> cm.putOrDecrement(s));
        t1.start();
        t2.start();
        synchronized (DEFAULT_KEY) {
            System.out.println("done waiting");
        }
        assertEquals(0, cm.getValue(s));
    }

    @Fuzz
    public void testAllInc(@InRange(minInt=MIN_ELEMENT, maxInt=MAX_ELEMENT) Integer input, @From(ScheduleGenerator.class) Schedule schedule) throws InterruptedException {
        CounterMap cm = new CounterMap();

        concurrency.Thread[] threads = new Thread[input];
        for(int c = 0; c < input; c++) {
            threads[c] = new Thread(() -> cm.putOrIncrement(DEFAULT_KEY));
            threads[c].start();
        }
        for(int c = 0; c < input; c++) {
            threads[c].wait();
        }

        assertEquals(input.intValue(), cm.getValue(DEFAULT_KEY));
    }
}
