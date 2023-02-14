package concurrency;

public class CounterMap {
    Integer updateMe;
    public final Object LOCK = "LOCK";

    public CounterMap() {
        updateMe = null;
    }

    public void putOrIncrement(String s) {
        //System.out.println("[Thread " + java.lang.Thread.currentThread() + "] putOrIncrement");
        if(containsKey(s)) {
            //System.out.println("finished containsKey");
            synchronized(LOCK) {
                updateMe = updateMe + 1;
            }

            //System.out.println("[putOrIncrement] key already present");
        } else {
            //System.out.println("finished containsKey");
            synchronized(LOCK) {
                updateMe = 1;
            }
            //System.out.println("[putOrIncrement] key not present");
        }
    }

    public void putOrDecrement(String s) {
        //System.out.println("[Thread " + java.lang.Thread.currentThread() + "] putOrDecrement");
        if(containsKey(s)) {
            //System.out.println("finished containsKey");
            synchronized(LOCK) {
                updateMe = updateMe - 1;
            }
            //System.out.println("[putOrDecrement] key already present");
        } else {
            //System.out.println("finished containsKey");
            synchronized(LOCK) {
                updateMe = -1;
            }
            //System.out.println("[putOrDecrement] key not present");
        }
    }

    public int getValue(String s) {
        synchronized (LOCK) {
            //System.out.println("getValue");
            if(updateMe == null) return Integer.MIN_VALUE;
            //System.out.println("got value " + updateMe);
            return updateMe;
        }
    }

    public boolean containsKey(String s) {
        synchronized (LOCK) {
            //System.out.println("containsKey");
            return updateMe != null;
        }
    }
}
