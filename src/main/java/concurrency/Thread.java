package concurrency;

public class Thread extends java.lang.Thread {
    public Thread(Runnable r) {
        super(r);
    }

    @Override
    public void start() {
        super.start();
    }
}
