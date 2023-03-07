package concurrency;

import cmu.pasta.cdiff.IndexedThread;

//TODO put this in the other repo?
public class Thread extends IndexedThread {
    public Thread(Runnable r) {
        super(r);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void run() {
        //needed in order to make created threads get instrumented by SingleSnoop
        super.run();
    }

    @Override
    public void newJoin() throws InterruptedException {
        super.newJoin();
    }
}
