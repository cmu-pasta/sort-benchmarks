package concurrency.experiments;

public class SynchronizedMethod {
    public volatile int x = 0;

    public synchronized void addX(SynchronizedMethod sm) {
        x += 4;
        sm.x += 2;
    }

    public synchronized void multX(SynchronizedMethod sm) {
        x *= 5;
        sm.x *= 3;
    }
}
