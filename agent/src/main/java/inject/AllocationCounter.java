package inject;

import java.util.HashMap;

public class AllocationCounter {
    /**
     * key = StackTraceElement[]
     */
    public static HashMap<String, Integer> lineCounter = new HashMap<>();
    public static HashMap<String, Integer> methodCounter = new HashMap<>();
    public static HashMap<String, Integer> classCounter = new HashMap<>();

    public void addCounts(StackTraceElement stackTrace, byte[] bytes) {
    }
}
