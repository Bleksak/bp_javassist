package inject;

import java.util.HashMap;
import java.util.Map;

public class AllocationCounter {
    /**
     * key = filename:line_number
     */
    public static HashMap<String, Integer> lineCounter = new HashMap<>();

    /**
     * key = filename:classname:methodname
     */
    // TODO: we may have to consider using one more item for key, becuase class can have multiple methods with the same name
    public static HashMap<String, Integer> methodCounter = new HashMap<>();

    /**
     * key = filename:classname
     */
    public static HashMap<String, Integer> classCounter = new HashMap<>();

    public static void addCounts(StackTraceElement stackTrace, byte[] bytes) {
        String lineCounterString = String.format("%s:%s", stackTrace.getFileName(), stackTrace.getLineNumber());
        String methodCounterString = String.format("%s:%s:%s", stackTrace.getFileName(), stackTrace.getClassName(), stackTrace.getMethodName());
        String classCounterString = String.format("%s:%s", stackTrace.getFileName(), stackTrace.getClassName());

        Integer lC = lineCounter.getOrDefault(lineCounterString, 0);
        lineCounter.put(lineCounterString, lC + bytes.length);

        Integer mC = methodCounter.getOrDefault(methodCounterString, 0);
        methodCounter.put(methodCounterString, mC + bytes.length);

        Integer cC = classCounter.getOrDefault(classCounterString, 0);
        classCounter.put(classCounterString, cC + bytes.length);
    }

    public static void printInfo() {

        System.out.println("\nApplication allocation summary: ");

        for(Map.Entry<String, Integer> entry : lineCounter.entrySet()) {
            String line = entry.getKey();
            int allocated = entry.getValue();

            System.out.format("%s allocated: %d bytes\n", line, allocated);
        }
        System.out.println();

        for(Map.Entry<String, Integer> entry : methodCounter.entrySet()) {
            String method = entry.getKey();
            int allocated = entry.getValue();

            System.out.format("%s allocated: %d bytes\n", method, allocated);
        }
        System.out.println();

        for(Map.Entry<String, Integer> entry : classCounter.entrySet()) {
            String clazz = entry.getKey();
            int allocated = entry.getValue();

            System.out.format("%s allocated: %d bytes\n", clazz, allocated);
        }
    }
}
