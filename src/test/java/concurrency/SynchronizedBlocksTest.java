package concurrency;

import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JQF.class)
public class SynchronizedBlocksTest {
    @Test
    public void test() throws InterruptedException {
        SynchronizedBlocks sb = new SynchronizedBlocks();
        Thread t1 = new Thread(sb::block1);
        Thread t2 = new Thread(sb::block2);
        t1.start();
        t2.start();
        Thread.sleep(8000);
        System.out.println("x: " + sb.x);
    }
}
