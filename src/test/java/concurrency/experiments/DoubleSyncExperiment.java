package concurrency.experiments;

import java.util.ArrayList;
import java.util.List;

public class DoubleSyncExperiment {
    private static List<String> l = new ArrayList<>();

    private static class MyThread extends Thread {
        @Override
        public void run() {
            synchronized("TEST") {
                synchronized ("TEST") {
                    try {
                        MyThread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    l.add("t1");
                }
            }
        }
    }

    public static void main(String[] args) {
        MyThread t1 = new MyThread();
        t1.start();
        synchronized("TEST") {
            synchronized ("TEST") {
                l.add("t1");
            }
        }
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(l);
    }
}
