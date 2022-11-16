package concurrency;

public class Thread {
    private java.lang.Thread t;

    public Thread(Runnable r) {
        t = new java.lang.Thread(r);
    }

    public void start() {
        t.start();
    }

    public java.lang.Thread getThread() {
        return t;
    }
}
