package concurrency.experiments;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrencyBytecode {
    public volatile String x;
    public String y;
    AtomicInteger z;
    ReentrantLock testLock;
    Condition testCond;

    public ConcurrencyBytecode(String x, String y, int z) {
        this.x = x;
        this.y = y;
        this.z = new AtomicInteger(z);
        this.testLock = new ReentrantLock();
        this.testCond = testLock.newCondition();
    }

    public void seeSynchronizedX() throws InterruptedException {
        synchronized (x) { System.out.println(x); }
        synchronized (x) {
            java.lang.Thread.yield();
            x = "X:Hello " + x;
        }
    }

    public void seeSynchronizedY() throws InterruptedException {
        synchronized (y) {
            y = "Y:World " + y;
        }
    }

    public synchronized void synchronizedMethodX() {
        x = x + " synchronized";
    }

    public synchronized void synchronizedMethodY() {
        y = y + " synchronized";
    }

    public void unsynchronizedMethodX() {
        x = x + " synchronized";
    }

    public void unsynchronizedMethodY() {
        y = y + " synchronized";
    }

    public void lockedMethodX() throws InterruptedException {
        testLock.lock();
        testCond.await();
        x = x + " unsynchronized";
        testCond.notify();
        testLock.unlock();
    }

    public void lockedMethodY() throws InterruptedException {
        testLock.lock();
        testCond.await();
        y = y + " unsynchronized";
        testCond.notify();
        testLock.unlock();
    }

    public void atomicUp() {
        System.out.println(z.getAndAdd(1));
    }
}
