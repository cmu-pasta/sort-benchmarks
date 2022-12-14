package concurrency;

import java.util.HashMap;
import java.util.Map;

public class CounterMap {
    private final Map<String, Integer> map;
    public final Object LOCK = "LOCK";

    public CounterMap() {
        map = new HashMap<>();
    }

    public void putOrIncrement(String s) {
        System.out.println("[Thread " + java.lang.Thread.currentThread() + "] putOrIncrement");
        if(containsKey(s)) {
            System.out.println("finished containsKey");
            putValue(s, getValue(s) + 1);
            System.out.println("[Thread " + java.lang.Thread.currentThread() + "] key already present");
        } else {
            System.out.println("finished containsKey");
            putValue(s, 1);
            System.out.println("[Thread " + java.lang.Thread.currentThread() + "] key not present");
        }
    }

    public void putOrDecrement(String s) {
        System.out.println("[Thread " + java.lang.Thread.currentThread() + "] putOrDecrement");
        if(containsKey(s)) {
            System.out.println("finished containsKey");
            putValue(s, getValue(s) - 1);
            System.out.println("[Thread " + java.lang.Thread.currentThread() + "] key already present");
        } else {
            System.out.println("finished containsKey");
            putValue(s, -1);
            System.out.println("[Thread " + java.lang.Thread.currentThread() + "] key not present");
        }
    }

    public int getValue(String s) {
        synchronized (LOCK) {
            System.out.println("[Thread " + java.lang.Thread.currentThread() + "] getValue");
            Integer toReturn;
            toReturn = map.get(s);
            if(toReturn == null) return Integer.MIN_VALUE;
            System.out.println("[Thread " + java.lang.Thread.currentThread() + "] got value " + toReturn);
            return toReturn;
        }
    }

    public void putValue(String s, Integer i) {
        synchronized (LOCK) {
            System.out.println("[Thread " + java.lang.Thread.currentThread() + "] putValue");
            map.put(s, i);
            System.out.println("[Thread " + java.lang.Thread.currentThread() + "] put value " + i);
        }
    }

    public boolean containsKey(String s) {
        synchronized (LOCK) {
            System.out.println("[Thread " + java.lang.Thread.currentThread() + "] containsKey");
            return map.containsKey(s);
        }
    }
}
