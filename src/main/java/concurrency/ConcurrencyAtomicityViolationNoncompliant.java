package concurrency;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//adapted from https://docs.aws.amazon.com/codeguru/detector-library/java/concurrency-atomicity-violation/
public class ConcurrencyAtomicityViolationNoncompliant {
    public final ConcurrentHashMap<String, String> concurrentMap;

    public ConcurrencyAtomicityViolationNoncompliant(Map<String, String> concurrentMap) {
        this.concurrentMap = new ConcurrentHashMap<>(concurrentMap);
    }

    public String getValue(String key) {
        // Noncompliant: the key could be removed from the map between the first call and the second one.
        synchronized ("LOCK") {
            System.out.println("ConcurrencyAtomicityViolationNoncompliant: " + key + " in " + concurrentMap);
            if (concurrentMap.containsKey(key)) {
                String value = concurrentMap.get(key);
                System.out.println("ConcurrencyAtomicityViolationNoncompliant: " + value.length());
                return value;
            }
            return "key not present";
        }
    }

    public void deleteValue(String key) {
        synchronized ("LOCK") {
            System.out.println("ConcurrencyAtomicityViolationNoncompliant: " + key + " in " + concurrentMap);
            concurrentMap.remove(key);
        }
    }
}
