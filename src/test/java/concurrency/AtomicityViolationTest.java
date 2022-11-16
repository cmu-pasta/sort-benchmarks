package concurrency;

import cmu.pasta.cdiff.Schedule;
import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.generator.Size;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;

import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(JQF.class)
public class AtomicityViolationTest {
    protected static final int MAX_SIZE = 160;

    @Fuzz
    public void testAtomicityViolation(@Size(max=MAX_SIZE) Map<String, String> input, @From(ScheduleGenerator.class) Schedule schedule) throws InterruptedException {
        ConcurrencyAtomicityViolationNoncompliant cavn = new ConcurrencyAtomicityViolationNoncompliant(input);
        String key = (String) input.keySet().toArray()[0];
        concurrency.Thread t1 = new concurrency.Thread(() -> assertEquals(input.get(key), cavn.getValue(key)));
        concurrency.Thread t2 = new concurrency.Thread(() -> cavn.deleteValue(key));
        t1.start();
        t2.start();
        java.lang.Thread.sleep(5000); //todo require sleep until done?
    }
}
