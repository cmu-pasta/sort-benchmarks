package concurrency.experiments;

//@RunWith(JQF.class)
public class ConcurrencyBytecodeTest {
    protected static final int MAX_SIZE = 160;

    //@Fuzz
    public void testConcurrencyBytecode(String forX, String forY, Integer forZ) {
        ConcurrencyBytecode cb = new ConcurrencyBytecode(forX, forY, forZ);

        Thread tSee1 = new Thread(() -> {
            try {
                cb.seeSynchronizedX();
                cb.seeSynchronizedY();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(cb.x + ", " + cb.y);
        });
        Thread tSee2 = new Thread(() -> {
            try {
                cb.seeSynchronizedX();
                cb.seeSynchronizedY();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(cb.x + ", " + cb.y);
        });
        Thread tSync1 = new Thread(() -> {
            cb.synchronizedMethodX();
            cb.synchronizedMethodY();
            System.out.println(cb.x + ", " + cb.y);
        });
        Thread tSync2 = new Thread(() -> {
            cb.synchronizedMethodX();
            cb.synchronizedMethodY();
            System.out.println(cb.x + ", " + cb.y);
        });
        Thread tUnsync1 = new Thread(() -> {
            cb.unsynchronizedMethodX();
            cb.unsynchronizedMethodY();
            System.out.println(cb.x + ", " + cb.y);
        });
        Thread tUnsync2 = new Thread(() -> {
            cb.unsynchronizedMethodX();
            cb.unsynchronizedMethodY();
            System.out.println(cb.x + ", " + cb.y);
        });

        tSee1.start();
        tSee2.start();
        tSync1.start();
        tSync2.start();
        tUnsync1.start();
        tUnsync2.start();

        Thread tAtomicUp1 = new Thread(cb::atomicUp);
        Thread tAtomicUp2 = new Thread(cb::atomicUp);

        tAtomicUp1.start();
        tAtomicUp2.start();
    }
}

