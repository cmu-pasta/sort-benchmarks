package concurrency;

import cmu.pasta.cdiff.StatefulThread;

//TODO put this in the other repo?
public class Thread extends StatefulThread {
    public Thread(Runnable r) {
        super(r);
    }

    @Override
    public void start() {
        super.start();
    }
}
