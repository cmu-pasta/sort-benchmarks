package concurrency;

import java.util.HashMap;

public class CounterMap {
    HashMap<String, Integer> updateMe;
    public final Object LOCK = "LOCK";

    public CounterMap() {
        updateMe = new HashMap<>();
    }

    public void putOrIncrement(String s) {
        if(containsKey(s)) {
            //System.out.println("finished containsKey");
            synchronized(LOCK) {
                updateMe.put("hello", updateMe.get("hello") + 1);
            }

            //System.out.println("[putOrIncrement] key already present");
        } else {
            //System.out.println("finished containsKey");
            synchronized(LOCK) {
                updateMe.put("hello", 1);
            }
            //System.out.println("[putOrIncrement] key not present");
        }
    }

    public void putOrDecrement(String s) {
        //System.out.println("[Thread " + java.lang.Thread.currentThread() + "] putOrDecrement");
        if(containsKey(s)) {
            //System.out.println("finished containsKey");
            synchronized(LOCK) {
                updateMe.put("hello", updateMe.get("hello") - 1);
            }
            //System.out.println("[putOrDecrement] key already present");
        } else {
            //System.out.println("finished containsKey");
            synchronized(LOCK) {
                updateMe.put("hello", -1);
            }
            //System.out.println("[putOrDecrement] key not present");
        }
    }

    public int getValue(String s) {
        synchronized (LOCK) {
            //System.out.println("getValue");
            if(!updateMe.containsKey("hello")) return Integer.MIN_VALUE;
            //System.out.println("got value " + updateMe);
            return updateMe.get("hello");
        }
    }

    public boolean containsKey(String s) {
        synchronized (LOCK) {
            //System.out.println("containsKey");
            return updateMe.containsKey("hello");
        }
    }
}
