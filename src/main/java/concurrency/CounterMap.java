package concurrency;

import java.util.HashMap;
import java.util.Map;

public class CounterMap {
    Integer updateMe;
    public final Object LOCK = "LOCK";

    public CounterMap() {
        updateMe = null;
    }

    public void putOrIncrement(String s) {
        System.out.println("[Thread " + java.lang.Thread.currentThread() + "] putOrIncrement");
        if(containsKey(s)) {
            System.out.println("finished containsKey");
            synchronized(LOCK) {
                updateMe = updateMe + 1;
            }

            System.out.println("[putOrIncrement] key already present");
        } else {
            System.out.println("finished containsKey");
            synchronized(LOCK) {
                updateMe = 1;
            }
            System.out.println("[putOrIncrement] key not present");
        }
    }

    public void putOrDecrement(String s) {
        System.out.println("[Thread " + java.lang.Thread.currentThread() + "] putOrDecrement");
        if(containsKey(s)) {
            System.out.println("finished containsKey");
            synchronized(LOCK) {
                updateMe = updateMe - 1;
            }
            //AND HERE???
            //is the blocking thing monitorEXIT?
            System.out.println("[putOrDecrement] key already present");
        } else {
            System.out.println("finished containsKey");
            synchronized(LOCK) {
                updateMe = -1;
            }
            System.out.println("[putOrDecrement] key not present");
            //SOMETHING BLOCKING HAPPENS HERE (?????)
        }
    }

    public int getValue(String s) {
        synchronized (LOCK) {
            System.out.println("getValue");
            if(updateMe == null) return Integer.MIN_VALUE;
            System.out.println("got value " + updateMe);
            return updateMe;
        }
    }

    /*public void putValue(String s, Integer i) {
        synchronized (LOCK) {
            System.out.println("[Thread " + java.lang.Thread.currentThread() + "] putValue");
            map.put(s, i);
            System.out.println("[Thread " + java.lang.Thread.currentThread() + "] put value " + i);
        }
    }*/

    public boolean containsKey(String s) {
        synchronized (LOCK) {
            System.out.println("containsKey");
            return updateMe != null;
        }
    }
}
