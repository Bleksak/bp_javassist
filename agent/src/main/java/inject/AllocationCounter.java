package inject;

import java.util.HashMap;
import java.util.Map;

public class AllocationCounter {
    /**
     * key = filename:line_number
     */
    public static HashMap<String, Long> lineCounter = new HashMap<>();

    /**
     * key = filename:classname:methodname
     */
    // TODO: we may have to consider using one more item for key, becuase class can have multiple methods with the same name
    public static HashMap<String, Long> methodCounter = new HashMap<>();

    /**
     * key = filename:classname
     */
    public static HashMap<String, Long> classCounter = new HashMap<>();

    public static void addCounts(StackTraceElement stackTrace, long size) {
        String lineCounterString = String.format("%s:%s", stackTrace.getFileName(), stackTrace.getLineNumber());
        String methodCounterString = String.format("%s:%s:%s", stackTrace.getFileName(), stackTrace.getClassName(), stackTrace.getMethodName());
        String classCounterString = String.format("%s:%s", stackTrace.getFileName(), stackTrace.getClassName());

        Long lC = lineCounter.getOrDefault(lineCounterString, 0l);
        lineCounter.put(lineCounterString, lC + size);

        Long mC = methodCounter.getOrDefault(methodCounterString, 0l);
        methodCounter.put(methodCounterString, mC + size);

        Long cC = classCounter.getOrDefault(classCounterString, 0l);
        classCounter.put(classCounterString, cC + size);
    }

    public static void printInfo() {

        System.out.println("\nApplication allocation summary: ");

        for(Map.Entry<String, Long> entry : lineCounter.entrySet()) {
            String line = entry.getKey();
            long allocated = entry.getValue();

            System.out.format("%s allocated: %d bytes\n", line, allocated);
        }
        System.out.println();

        for(Map.Entry<String, Long> entry : methodCounter.entrySet()) {
            String method = entry.getKey();
            long allocated = entry.getValue();

            System.out.format("%s allocated: %d bytes\n", method, allocated);
        }
        System.out.println();

        for(Map.Entry<String, Long> entry : classCounter.entrySet()) {
            String clazz = entry.getKey();
            long allocated = entry.getValue();

            System.out.format("%s allocated: %d bytes\n", clazz, allocated);
        }
    }
}
