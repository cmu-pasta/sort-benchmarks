package concurrency.experiments;

//@RunWith(JQF.class)
public class SynchronizedMethodTest {
    //@Test
    public void test() throws InterruptedException {
        SynchronizedMethod sm1 = new SynchronizedMethod();
        SynchronizedMethod sm2 = new SynchronizedMethod();
        Thread t1 = new Thread(() -> sm1.addX(sm2));
        Thread t2 = new Thread(() -> sm1.multX(sm2));
        t1.start();
        t2.start();
        java.lang.Thread.sleep(8000);
        System.out.println("sm1.x: " + sm1.x);
        System.out.println("sm2.x: " + sm2.x);

        //TODO don't use threadIDs
        for(int c = 0; c < 2; c++) {
            java.lang.Thread x = new java.lang.Thread();
            x.start(); //execution indexing
            //JQF has ExecutionIndexingGuidance
        }

        //have a spinning thread that checks to make sure at least one thread is running
        //if no thread is running, then unlock one that we control
        //problems if nobody running and we have no control
    }
}
